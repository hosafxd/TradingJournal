from django.shortcuts import render
import json
from rest_framework import viewsets, permissions, generics, status
from rest_framework.response import Response
from rest_framework.views import APIView
from rest_framework.decorators import api_view, parser_classes
from .models import Account, Trade, SetupStrategy, EntryType
from .serializers import (
    AccountSerializer, TradeSerializer, SetupStrategySerializer,
    EntryTypeSerializer, UserRegistrationSerializer, UserSerializer,
    DashboardMetricsSerializer, EquityDataPointSerializer, PnlOverTimeDataPointSerializer,
    MonthlyCalendarSerializer
)
from django.contrib.auth.models import User
from django.db import models, transaction
from django.db.models import Sum, Avg, Count, Max, Min
from django.contrib.contenttypes.models import ContentType
from django.db.models.functions import TruncDay, TruncWeek, TruncMonth, TruncYear
from decimal import Decimal, ROUND_HALF_UP
import calendar
from datetime import date
from rest_framework.authtoken.models import Token 
from .filters import TradeFilter # Import the new TradeFilter
from rest_framework.parsers import MultiPartParser, FormParser, JSONParser


# Create your views here.

class UserRegistrationView(generics.CreateAPIView):
    queryset = User.objects.all()
    serializer_class = UserRegistrationSerializer
    permission_classes = [permissions.AllowAny] 

    def create(self, request, *args, **kwargs):
        serializer = self.get_serializer(data=request.data)
        serializer.is_valid(raise_exception=True) # This will raise validation errors leading to 400
        user = serializer.save() 
        
        try:
            token = Token.objects.get(user=user)
        except Token.DoesNotExist:
            token = Token.objects.create(user=user)
            
        headers = self.get_success_headers(serializer.data)
        return Response(
            {
                "user": UserSerializer(user, context=self.get_serializer_context()).data,
                "token": token.key
            },
            status=status.HTTP_201_CREATED,
            headers=headers
        )

class UserDetailsView(generics.RetrieveAPIView):
    """
    API endpoint that returns details of the currently authenticated user.
    """
    serializer_class = UserSerializer
    permission_classes = [permissions.IsAuthenticated]

    def get_object(self):
        return self.request.user


class IsOwnerOrReadOnly(permissions.BasePermission):
    """
    Custom permission to only allow owners of an object to edit it.
    Assumes the model instance has a `user` attribute.
    """
    def has_object_permission(self, request, view, obj):
        # Read permissions are allowed to any request,
        # so we'll always allow GET, HEAD or OPTIONS requests.
        if request.method in permissions.SAFE_METHODS:
            return True

        # Write permissions are only allowed to the owner of the snippet.
        # For Account, Trade (via account.user), SetupStrategy, EntryType
        if hasattr(obj, 'user'): # For Account, SetupStrategy, EntryType
            return obj.user == request.user
        if hasattr(obj, 'account'): # For Trade
            return obj.account.user == request.user
        if hasattr(obj, 'trade'): # For Screenshot
            return obj.trade.account.user == request.user
        return False


class AccountViewSet(viewsets.ModelViewSet):
    """
    API endpoint that allows accounts to be viewed or edited.
    """
    serializer_class = AccountSerializer
    permission_classes = [permissions.IsAuthenticated, IsOwnerOrReadOnly]

    def get_queryset(self):
        """
        This view should return a list of all the accounts
        for the currently authenticated user.
        """
        return Account.objects.filter(user=self.request.user)

    def perform_create(self, serializer):
        serializer.save(user=self.request.user)


class SetupStrategyViewSet(viewsets.ModelViewSet):
    """
    API endpoint that allows setup strategies to be viewed or edited.
    """
    parser_classes = (MultiPartParser, FormParser)
    serializer_class = SetupStrategySerializer
    permission_classes = [permissions.IsAuthenticated, IsOwnerOrReadOnly]

    def get_queryset(self):
        """
        This view should return a list of all the setup strategies
        for the currently authenticated user or public ones (if user is null).
        """
        # Allows user to see their own strategies and strategies with no assigned user (public)
        return SetupStrategy.objects.filter(models.Q(user=self.request.user) | models.Q(user__isnull=True))

    def perform_create(self, serializer):
        serializer.save(user=self.request.user)

class EntryTypeViewSet(viewsets.ModelViewSet):
    """
    API endpoint that allows entry types to be viewed or edited.
    """
    parser_classes = (MultiPartParser, FormParser)
    serializer_class = EntryTypeSerializer
    permission_classes = [permissions.IsAuthenticated, IsOwnerOrReadOnly]

    def get_queryset(self):
        """
        This view should return a list of all the entry types
        for the currently authenticated user or public ones.
        """
        return EntryType.objects.filter(models.Q(user=self.request.user) | models.Q(user__isnull=True))

    def perform_create(self, serializer):
        serializer.save(user=self.request.user)

class TradeViewSet(viewsets.ModelViewSet):
    """
    API endpoint that allows trades to be viewed or edited.
    """
    serializer_class = TradeSerializer
    permission_classes = [permissions.IsAuthenticated, IsOwnerOrReadOnly]
    filterset_class = TradeFilter 

    def get_queryset(self):
        """
        This view should return a list of all the trades
        for accounts belonging to the currently authenticated user.
        """
        return Trade.objects.filter(account__user=self.request.user).order_by('-entry_date')

# --- New Views for Metrics and Charts ---

class DashboardMetricsView(APIView):
    permission_classes = [permissions.IsAuthenticated]

    def get(self, request, *args, **kwargs):
        account_id = request.query_params.get('account_id')
        user = request.user
        account_name = "All Accounts"

        trades_qs = Trade.objects.filter(account__user=user, returns__isnull=False) # Only closed trades

        if account_id:
            try:
                account = Account.objects.get(id=account_id, user=user)
                trades_qs = trades_qs.filter(account=account)
                account_name = account.name
            except Account.DoesNotExist:
                return Response({"error": "Account not found or access denied."}, status=status.HTTP_404_NOT_FOUND)
        
        if not trades_qs.exists():
             # Return default/empty metrics if no trades
            empty_metrics = {
                "total_pnl": Decimal('0.00'), "total_trades": 0, "winning_trades": 0,
                "losing_trades": 0, "breakeven_trades": 0, "win_rate": 0.0,
                "average_pnl_per_trade": Decimal('0.00'), "average_winning_trade": None,
                "average_losing_trade": None, "profit_factor": None,
                "largest_winning_trade": None, "largest_losing_trade": None,
                "account_name": account_name, "account_id": int(account_id) if account_id else None
            }
            serializer = DashboardMetricsSerializer(empty_metrics)
            return Response(serializer.data)


        total_pnl = trades_qs.aggregate(Sum('returns'))['returns__sum'] or Decimal('0.00')
        total_trades = trades_qs.count()

        winning_trades_qs = trades_qs.filter(returns__gt=0)
        losing_trades_qs = trades_qs.filter(returns__lt=0)
        
        winning_trades_count = winning_trades_qs.count()
        losing_trades_count = losing_trades_qs.count()
        breakeven_trades_count = trades_qs.filter(returns=0).count()

        win_rate = (winning_trades_count / total_trades) * 100 if total_trades > 0 else 0.0
        
        average_pnl_per_trade = total_pnl / total_trades if total_trades > 0 else Decimal('0.00')

        average_winning_trade = winning_trades_qs.aggregate(Avg('returns'))['returns__avg']
        average_losing_trade = losing_trades_qs.aggregate(Avg('returns'))['returns__avg']

        gross_profit = winning_trades_qs.aggregate(Sum('returns'))['returns__sum'] or Decimal('0.00')
        gross_loss = abs(losing_trades_qs.aggregate(Sum('returns'))['returns__sum'] or Decimal('0.00'))
        profit_factor = gross_profit / gross_loss if gross_loss > 0 else None

        largest_winning_trade = trades_qs.aggregate(Max('returns'))['returns__max']
        largest_losing_trade = trades_qs.aggregate(Min('returns'))['returns__min']


        metrics = {
            "total_pnl": total_pnl,
            "total_trades": total_trades,
            "winning_trades": winning_trades_count,
            "losing_trades": losing_trades_count,
            "breakeven_trades": breakeven_trades_count,
            "win_rate": round(win_rate, 2),
            "average_pnl_per_trade": average_pnl_per_trade.quantize(Decimal('0.01'), rounding=ROUND_HALF_UP) if average_pnl_per_trade else Decimal('0.00'),
            "average_winning_trade": average_winning_trade.quantize(Decimal('0.01'), rounding=ROUND_HALF_UP) if average_winning_trade else None,
            "average_losing_trade": average_losing_trade.quantize(Decimal('0.01'), rounding=ROUND_HALF_UP) if average_losing_trade else None,
            "profit_factor": round(profit_factor, 2) if profit_factor is not None else None,
            "largest_winning_trade": largest_winning_trade,
            "largest_losing_trade": largest_losing_trade,
            "account_name": account_name,
            "account_id": int(account_id) if account_id else None
        }

        serializer = DashboardMetricsSerializer(metrics)
        return Response(serializer.data)


class EquityCurveDataView(APIView):
    permission_classes = [permissions.IsAuthenticated]

    def get(self, request, *args, **kwargs):
        account_id = request.query_params.get('account_id')
        if not account_id:
            # If no account is selected, return an empty list for the chart.
            return Response([], status=status.HTTP_200_OK)

        try:
            account = Account.objects.get(id=account_id, user=request.user)
        except Account.DoesNotExist:
            return Response({"error": "Account not found or access denied."}, status=status.HTTP_404_NOT_FOUND)

        # We need a starting point for the equity curve, which is the initial balance.
        # Trades should have `current_balance_after_trade` populated correctly.
        # For a true equity curve, we'd ideally track balance changes chronologically.
        
        # Option 1: Use `current_balance_after_trade` from each trade.
        # This assumes `current_balance_after_trade` is the account balance *after* that trade.
        trades = Trade.objects.filter(
            account=account, 
            entry_date__isnull=False, 
            current_balance_after_trade__isnull=False
        ).order_by('entry_date')

        if not trades.exists():
            # If no trades, equity curve starts and stays at initial balance (or current if no trades ever)
            # For simplicity, returning an empty list or initial balance point.
            # A better approach might be to return the initial balance at the account creation time.
            initial_point = [{"date": account.created_at, "balance": account.initial_balance}]
            serializer = EquityDataPointSerializer(initial_point, many=True)
            return Response(serializer.data)


        data_points = [{"date": account.created_at, "balance": account.initial_balance}] 
        data_points.extend([
            {"date": trade.entry_date, "balance": trade.current_balance_after_trade}
            for trade in trades
        ])
        
        serializer = EquityDataPointSerializer(data_points, many=True)
        return Response(serializer.data)


class PnlOverTimeDataView(APIView):
    permission_classes = [permissions.IsAuthenticated]

    def get(self, request, *args, **kwargs):
        account_id = request.query_params.get('account_id')
        period_type = request.query_params.get('period', 'monthly').lower() # daily, weekly, monthly, yearly
        user = request.user

        trades_qs = Trade.objects.filter(account__user=user, returns__isnull=False, entry_date__isnull=False)

        if account_id:
            try:
                account = Account.objects.get(id=account_id, user=user)
                trades_qs = trades_qs.filter(account=account)
            except Account.DoesNotExist:
                return Response({"error": "Account not found or access denied."}, status=status.HTTP_404_NOT_FOUND)

        if period_type == 'daily':
            trunc_func = TruncDay('entry_date')
        elif period_type == 'weekly':
            trunc_func = TruncWeek('entry_date')
        elif period_type == 'yearly':
            trunc_func = TruncYear('entry_date')
        else: # Default to monthly
            trunc_func = TruncMonth('entry_date')
            period_type = 'monthly'


        pnl_data_qs = trades_qs.annotate(
            period_datetime=trunc_func # Keep original datetime for ordering
        ).values('period_datetime').annotate(
            pnl=Sum('returns')
        ).order_by('period_datetime')
        
        # Convert datetime to date for serializer
        pnl_data = [
            {'period': item['period_datetime'].date(), 'pnl': item['pnl']}
            for item in pnl_data_qs
        ]
        
        serializer = PnlOverTimeDataPointSerializer(pnl_data, many=True)
        return Response(serializer.data)

# --- New View for Calendar Data ---

class CalendarDataView(APIView):
    permission_classes = [permissions.IsAuthenticated]
    
    def get_day_status(self, pnl):
        if pnl > 0:
            return "WINNING_DAY"
        elif pnl < 0:
            return "LOSING_DAY"
        else: # pnl == 0
            return "BREAKEVEN_DAY"

    def get(self, request, *args, **kwargs):
        try:
            year = int(request.query_params.get('year'))
            month = int(request.query_params.get('month'))
        except (TypeError, ValueError):
            return Response({"error": "Year and month parameters are required and must be integers."},
                            status=status.HTTP_400_BAD_REQUEST)

        account_id_str = request.query_params.get('account_id')
        user = request.user
        account_name = "All Accounts"
        target_account = None

        trades_base_qs = Trade.objects.filter(
            account__user=user,
            entry_date__year=year,
            entry_date__month=month,
            returns__isnull=False # Only consider closed trades with P&L
        )

        if account_id_str:
            try:
                account_id = int(account_id_str)
                target_account = Account.objects.get(id=account_id, user=user)
                trades_base_qs = trades_base_qs.filter(account=target_account)
                account_name = target_account.name
            except (Account.DoesNotExist, ValueError):
                return Response({"error": "Account not found or invalid account_id."},
                                status=status.HTTP_404_NOT_FOUND)

        # Aggregate data per day
        daily_aggregates = trades_base_qs.annotate(
            day=TruncDay('entry_date')
        ).values('day').annotate(
            daily_pnl=Sum('returns'),
            trade_count=Count('id')
        ).order_by('day')

        daily_data_map = {
            agg['day'].date() : { # Convert datetime to date for key
                'total_pnl': agg['daily_pnl'],
                'trade_count': agg['trade_count']
            } for agg in daily_aggregates
        }
        
        # Get strategies used per day
        for day_date, data in daily_data_map.items():
            strategies = list(
                trades_base_qs.filter(entry_date__date=day_date, setup_strategy__isnull=False)
                .values_list('setup_strategy__name', flat=True)
                .distinct()
            )
            data['strategies_used'] = strategies
            data['day_status'] = self.get_day_status(data['total_pnl'])


        # Prepare list of all days in the month for the calendar
        num_days_in_month = calendar.monthrange(year, month)[1]
        all_month_days_data = []
        
        total_monthly_pnl = Decimal('0.00')
        total_monthly_trades = 0
        winning_days_count = 0
        losing_days_count = 0
        breakeven_days_count = 0

        for day_num in range(1, num_days_in_month + 1):
            current_date = date(year, month, day_num)
            if current_date in daily_data_map:
                day_entry_data = daily_data_map[current_date]
                entry = {
                    'date': current_date,
                    'total_pnl': day_entry_data['total_pnl'],
                    'trade_count': day_entry_data['trade_count'],
                    'strategies_used': day_entry_data['strategies_used'],
                    'day_status': day_entry_data['day_status']
                }
                all_month_days_data.append(entry)
                
                total_monthly_pnl += day_entry_data['total_pnl']
                total_monthly_trades += day_entry_data['trade_count']
                if day_entry_data['day_status'] == "WINNING_DAY":
                    winning_days_count += 1
                elif day_entry_data['day_status'] == "LOSING_DAY":
                    losing_days_count += 1
                elif day_entry_data['day_status'] == "BREAKEVEN_DAY": # PNL is exactly 0
                    breakeven_days_count +=1
            # else: # Day with no trades, could optionally add an entry
            #     all_month_days_data.append({
            #         'date': current_date,
            #         'total_pnl': Decimal('0.00'),
            #         'trade_count': 0,
            #         'strategies_used': [],
            #         'day_status': "NO_TRADES"
            #     })


        response_data = {
            'year': year,
            'month': month,
            'account_name': account_name,
            'total_pnl': total_monthly_pnl,
            'total_trades': total_monthly_trades,
            'winning_days': winning_days_count,
            'losing_days': losing_days_count,
            'breakeven_days': breakeven_days_count,
            'days_with_trades': all_month_days_data,
        }

        return Response(response_data)


class ScreenshotView(APIView):
    """API endpoint for handling screenshots as documentation blocks."""
    parser_classes = (MultiPartParser, FormParser)
    permission_classes = [permissions.IsAuthenticated]

    def post(self, request, *args, **kwargs):
        # Extract trade_id from request data
        trade_id = request.data.get('trade')
        if not trade_id:
            return Response({'tradeId': ['This field is required.']}, status=status.HTTP_400_BAD_REQUEST)
        
        try:
            trade = Trade.objects.get(id=trade_id)
            # Check if user has permission to modify this trade
            if trade.account.user != request.user:
                return Response({'detail': 'You do not have permission to add screenshots to this trade.'}, 
                                status=status.HTTP_403_FORBIDDEN)
        except Trade.DoesNotExist:
            return Response({'trade': ['Trade not found.']}, status=status.HTTP_404_NOT_FOUND)
        
        # Get the image file from request
        image_file = request.FILES.get('image')
        if not image_file:
            return Response({'image': ['This field is required.']}, status=status.HTTP_400_BAD_REQUEST)
        
        # Get caption if provided
        caption = request.data.get('caption', '')
        
        # Get the current highest order value for this trade's documentation blocks
        current_max_order = trade.documentation_blocks.aggregate(Max('order'))['order__max'] or -1
        next_order = current_max_order + 1
        
        # Create a new documentation block with the image
        content_type = ContentType.objects.get_for_model(Trade)
        doc_block = DocumentationBlock.objects.create(
            content_type=content_type,
            object_id=trade.id,
            block_type='image',
            image_content=image_file,
            text_content=caption if caption else None,
            order=next_order
        )
        
        # Return the created documentation block as a "screenshot" for backward compatibility
        return Response({
            'id': doc_block.id,
            'tradeId': trade.id,
            'image': doc_block.image_content.url if doc_block.image_content else None,
            'caption': doc_block.text_content,
            'uploadedAt': doc_block.id  # Using ID as a proxy for upload time
        }, status=status.HTTP_201_CREATED)

    def delete(self, request, pk=None, *args, **kwargs):
        try:
            doc_block = DocumentationBlock.objects.get(id=pk)
            
            # Check if user has permission to delete this documentation block
            content_object = doc_block.content_object
            if hasattr(content_object, 'account'):
                if content_object.account.user != request.user:
                    return Response({'detail': 'You do not have permission to delete this screenshot.'}, 
                                    status=status.HTTP_403_FORBIDDEN)
            elif hasattr(content_object, 'user'):
                if content_object.user != request.user:
                    return Response({'detail': 'You do not have permission to delete this screenshot.'}, 
                                    status=status.HTTP_403_FORBIDDEN)
            else:
                return Response({'detail': 'Cannot determine ownership of this screenshot.'}, 
                                status=status.HTTP_403_FORBIDDEN)
            
            # Delete the documentation block
            doc_block.delete()
            return Response(status=status.HTTP_204_NO_CONTENT)
            
        except DocumentationBlock.DoesNotExist:
            return Response({'detail': 'Screenshot not found.'}, status=status.HTTP_404_NOT_FOUND)
