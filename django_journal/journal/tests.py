from django.test import TestCase
from django.urls import reverse
from django.contrib.auth.models import User
from rest_framework.test import APIClient, APITestCase
from rest_framework import status
from decimal import Decimal
from datetime import date, datetime, timedelta, timezone
import calendar
from django.core.files.uploadedfile import SimpleUploadedFile


from .models import Account, Trade, SetupStrategy, EntryType, Screenshot
from .serializers import (
    UserRegistrationSerializer, AccountSerializer, TradeSerializer,
    SetupStrategySerializer, EntryTypeSerializer, MonthlyCalendarSerializer,
    DashboardMetricsSerializer, ScreenshotSerializer # Added ScreenshotSerializer
)

# Helper function to create a user
def create_user(username="testuser", password="testpassword123", email="test@example.com"):
    return User.objects.create_user(username=username, password=password, email=email)

class ModelTests(TestCase):

    def setUp(self):
        self.user = create_user()
        self.account = Account.objects.create(
            user=self.user,
            name="Test Account",
            initial_balance=Decimal("1000.00"), # Test for "Set custom starting balance"
            current_balance=Decimal("1000.00")
        )
        self.strategy = SetupStrategy.objects.create(user=self.user, name="Breakout", description="Detailed strategy documentation here.") # Test for "Strategy documentation"
        self.entry_type = EntryType.objects.create(user=self.user, name="Pullback", description="Entry on pullback to MA.")
        # Note: "Custom risk parameters" beyond initial balance are not in the Account model yet.
        # Note: "Trading rules" model/feature is not yet implemented.
        # Note: "Symbol management" model/feature is not yet implemented.

    def test_account_creation(self):
        self.assertEqual(self.account.name, "Test Account")
        self.assertEqual(self.account.user.username, "testuser")
        self.assertEqual(str(self.account), "Test Account (testuser)")

    def test_setup_strategy_creation_and_documentation(self):
        self.assertEqual(self.strategy.name, "Breakout")
        self.assertEqual(self.strategy.description, "Detailed strategy documentation here.")
        self.assertEqual(str(self.strategy), "Breakout")

    def test_entry_type_creation(self):
        self.assertEqual(self.entry_type.name, "Pullback")
        self.assertEqual(str(self.entry_type), "Pullback")

    def test_trade_creation_all_fields_and_status(self):
        now = datetime.now(timezone.utc)
        entry_dt = now - timedelta(days=1, hours=2)
        exit_dt = now - timedelta(days=1, hours=1)

        trade = Trade.objects.create(
            account=self.account,
            symbol="EURUSD", # Test for "Symbol"
            entry_date=entry_dt, # Test for "Date"
            exit_date=exit_dt,
            entry_price=Decimal("1.12000"), # Test for "Entry Price"
            exit_price=Decimal("1.12500"), # Test for "Exit Price"
            size=Decimal("10000"), # Test for "Size"
            side="BUY", # Test for "Side (LONG/SHORT)"
            returns=Decimal("50.00"), # Test for "Returns"
            current_balance_after_trade=self.account.initial_balance + Decimal("50.00"), # Test for "Balance"
            notes="Good entry based on news.", # Test for "Attach notes to trades"
            setup_strategy=self.strategy, # Test for "Setup Strategy"
            entry_type=self.entry_type # Test for "Entry Type"
        )
        self.assertEqual(trade.symbol, "EURUSD")
        self.assertEqual(str(trade), f"BUY EURUSD @ 1.12000 on {entry_dt.strftime('%Y-%m-%d')}")
        self.assertEqual(trade.status, "WIN")
        self.assertEqual(trade.duration, exit_dt - entry_dt) # Test for "Duration"
        self.assertEqual(trade.notes, "Good entry based on news.")
        self.assertEqual(trade.setup_strategy, self.strategy)
        self.assertEqual(trade.entry_type, self.entry_type)

    def test_trade_save_calculates_duration(self):
        now = datetime.now(timezone.utc)
        entry_dt = now - timedelta(minutes=30)
        exit_dt = now
        trade = Trade(
            account=self.account,
            symbol="BTCUSD",
            entry_date=entry_dt,
            exit_date=exit_dt, # Set exit_date before save
            entry_price=Decimal("30000"),
            exit_price=Decimal("30100"),
            size=Decimal("0.1"),
            side="BUY",
            returns=Decimal("10.00")
        )
        trade.save() # Duration should be calculated here
        self.assertEqual(trade.duration, exit_dt - entry_dt)

        trade_no_exit = Trade(account=self.account, symbol="ETHUSD", entry_date=now, entry_price=2000, size=1, side="BUY")
        trade_no_exit.save()
        self.assertIsNone(trade_no_exit.duration)


    def test_screenshot_creation(self):
        trade = Trade.objects.create(account=self.account, symbol="NVDA", entry_date=datetime.now(timezone.utc), entry_price=Decimal("300"), size=1, side="BUY")
        # Note: ImageField requires a file. For robust testing of "Attach screenshots",
        # use SimpleUploadedFile as shown in APITests.
        screenshot = Screenshot.objects.create(trade=trade, caption="Entry chart") # Test for "Attach screenshots"
        self.assertEqual(screenshot.caption, "Entry chart")
        self.assertEqual(str(screenshot), f"Screenshot for Trade ID {trade.id} - Entry chart")


class SerializerTests(TestCase):
    def setUp(self):
        self.user = create_user()
        self.account = Account.objects.create(user=self.user, name="Main Account", initial_balance=1000, current_balance=1000)
        self.strategy = SetupStrategy.objects.create(user=self.user, name="Trend Following", description="Follow the trend.")
        self.entry_type = EntryType.objects.create(user=self.user, name="Breakout Entry", description="Enter on breakout.")
        
        self.user_data = {"username": "newuser", "email": "new@example.com", "password": "newpassword123", "password2": "newpassword123"}
        self.account_data = {"name": "Serializer Test Account", "initial_balance": "2000.00"}
        self.trade_data = {
            "account": self.account.pk,
            "symbol": "MSFT",
            "entry_date": datetime.now(timezone.utc).isoformat(),
            "exit_date": (datetime.now(timezone.utc) + timedelta(hours=1)).isoformat(),
            "entry_price": "250.00",
            "exit_price": "255.00",
            "size": "10",
            "side": "BUY",
            "returns": "50.00",
            "notes": "Test trade notes",
            "setup_strategy": self.strategy.pk,
            "entry_type": self.entry_type.pk
        }
        # Mock request for serializers that need it (e.g., for CurrentUserDefault or context)
        class MockRequest:
            def __init__(self, user):
                self.user = user
        self.mock_request = MockRequest(user=self.user)


    def test_user_registration_serializer_valid(self):
        serializer = UserRegistrationSerializer(data=self.user_data)
        self.assertTrue(serializer.is_valid(), serializer.errors)
        user = serializer.save()
        self.assertEqual(User.objects.count(), 2) 
        self.assertEqual(user.username, "newuser")
        self.assertTrue(hasattr(user, 'auth_token')) # Check token is created


    def test_user_registration_serializer_password_mismatch(self):
        invalid_data = self.user_data.copy()
        invalid_data["password2"] = "wrongpassword"
        serializer = UserRegistrationSerializer(data=invalid_data)
        self.assertFalse(serializer.is_valid())
        self.assertIn("password", serializer.errors)

    def test_user_registration_serializer_existing_email(self):
        create_user(username="anotheruser", email="existing@example.com") # Create a user with this email first
        invalid_data = self.user_data.copy()
        invalid_data["username"] = "anothernewuser" # Ensure username is different to isolate email validation
        invalid_data["email"] = "existing@example.com"
        serializer = UserRegistrationSerializer(data=invalid_data)
        self.assertFalse(serializer.is_valid())
        self.assertIn("email", serializer.errors)

    def test_account_serializer_create(self):
        serializer = AccountSerializer(data=self.account_data, context={'request': self.mock_request})
        self.assertTrue(serializer.is_valid(), serializer.errors)
        # In perform_create of AccountViewSet, user is set from request.user
        # For direct serializer test, if user is not part of data, it needs to be passed to save()
        account = serializer.save(user=self.user) 
        self.assertEqual(account.name, "Serializer Test Account")
        self.assertEqual(account.user, self.user)
        self.assertEqual(account.current_balance, account.initial_balance) # Test "Set custom starting balance" via serializer

    def test_trade_serializer_valid_all_fields(self): # Renamed for clarity
        serializer = TradeSerializer(data=self.trade_data, context={'request': self.mock_request})
        self.assertTrue(serializer.is_valid(), serializer.errors)
        trade = serializer.save() # `perform_create` in ViewSet would set account if not passed
        self.assertEqual(trade.symbol, "MSFT")
        self.assertEqual(trade.setup_strategy, self.strategy)
        self.assertEqual(trade.entry_type, self.entry_type)
        self.assertEqual(trade.notes, "Test trade notes")
        self.assertEqual(trade.status, "WIN") # Status should be derived

    def test_trade_serializer_invalid_account_owner(self):
        other_user = create_user(username="otheruser", email="other@example.com", password="password")
        other_account = Account.objects.create(user=other_user, name="Other Account", initial_balance=100, current_balance=100)
        
        invalid_trade_data = self.trade_data.copy()
        invalid_trade_data["account"] = other_account.pk # Trade for another user's account

        serializer = TradeSerializer(data=invalid_trade_data, context={'request': self.mock_request})
        self.assertFalse(serializer.is_valid())
        self.assertIn("account", serializer.errors) # Validation is in TradeSerializer.validate_account

    def test_setup_strategy_serializer_with_documentation(self): # Renamed for clarity
        data = {"name": "New Strategy", "description": "Detailed documentation for this new strategy."}
        serializer = SetupStrategySerializer(data=data, context={'request': self.mock_request})
        self.assertTrue(serializer.is_valid(), serializer.errors)
        strategy = serializer.save(user=self.user)
        self.assertEqual(strategy.name, "New Strategy")
        self.assertEqual(strategy.description, "Detailed documentation for this new strategy.") # Test "Strategy documentation"
        self.assertEqual(strategy.user, self.user)

    def test_monthly_calendar_serializer(self):
        # Test data for the serializer
        calendar_data = {
            "year": 2023, "month": 10, "account_id": self.account.id, "account_name": self.account.name,
            "total_monthly_pnl": Decimal("150.50"), "total_monthly_trades": 5,
            "winning_days": 3, "losing_days": 1, "breakeven_days": 0,
            "days_with_trades": [
                {"date": date(2023, 10, 1), "total_pnl": Decimal("100.00"), "trade_count": 2, "strategies_used": ["Trend Following"], "day_status": "WINNING_DAY"},
                {"date": date(2023, 10, 2), "total_pnl": Decimal("-50.00"), "trade_count": 1, "strategies_used": ["Breakout Entry"], "day_status": "LOSING_DAY"},
                {"date": date(2023, 10, 3), "total_pnl": Decimal("100.50"), "trade_count": 2, "strategies_used": ["Trend Following"], "day_status": "WINNING_DAY"}
            ]
        }
        serializer = MonthlyCalendarSerializer(data=calendar_data)
        self.assertTrue(serializer.is_valid(), serializer.errors)

    def test_dashboard_metrics_serializer(self):
        metrics_data = {
            "total_pnl": Decimal("1234.56"), "total_trades": 100, "winning_trades": 60, "losing_trades": 35, "breakeven_trades": 5,
            "win_rate": 60.0, "average_pnl_per_trade": Decimal("12.35"),
            "average_winning_trade": Decimal("50.00"), "average_losing_trade": Decimal("-30.00"),
            "profit_factor": 1.67, "largest_winning_trade": Decimal("200.00"), "largest_losing_trade": Decimal("-100.00"),
            "account_name": "Test Account", "account_id": 1
        }
        serializer = DashboardMetricsSerializer(data=metrics_data)
        self.assertTrue(serializer.is_valid(), serializer.errors)


class APITests(APITestCase):
    def setUp(self):
        self.user1 = create_user(username="apiuser1", password="apipassword123", email="api1@example.com")
        self.user2 = create_user(username="apiuser2", password="apipassword123", email="api2@example.com")
        
        self.client = APIClient()
        response = self.client.post(reverse("api-token-auth"), {"username": "apiuser1", "password": "apipassword123"})
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.token1 = response.data["token"]
        self.client.credentials(HTTP_AUTHORIZATION=f"Token {self.token1}")

        self.account1_user1 = Account.objects.create(user=self.user1, name="User1 Account1", initial_balance=Decimal("5000"), current_balance=Decimal("5000"))
        self.account2_user1 = Account.objects.create(user=self.user1, name="User1 Account2", initial_balance=Decimal("2000"), current_balance=Decimal("2000"))
        self.account1_user2 = Account.objects.create(user=self.user2, name="User2 Account1", initial_balance=Decimal("3000"), current_balance=Decimal("3000"))

        self.strategy_user1 = SetupStrategy.objects.create(user=self.user1, name="User1 Strat", description="User1's main strategy.")
        self.strategy_public = SetupStrategy.objects.create(user=None, name="Public Strat", description="A public strategy for all.")
        self.entry_type_user1 = EntryType.objects.create(user=self.user1, name="User1 Entry", description="User1's entry type.")

    def test_multi_account_management_list_and_data_independence(self):
        url = reverse("account-list")
        response = self.client.get(url)
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        # User1 should only see their 2 accounts
        self.assertEqual(response.data['count'], 2)
        account_names = [acc['name'] for acc in response.data['results']]
        self.assertIn(self.account1_user1.name, account_names)
        self.assertIn(self.account2_user1.name, account_names)
        self.assertNotIn(self.account1_user2.name, account_names) # Verifies data independence

        # Create a trade in user1's account
        Trade.objects.create(
            account=self.account1_user1, symbol="T1",
            entry_date=datetime.now(timezone.utc), exit_date=datetime.now(timezone.utc),
            entry_price=Decimal("10.00"), # <<<< ENSURE THIS IS PRESENT
            size=Decimal("1.0"),          # <<<< ENSURE THIS IS PRESENT
            side="BUY",                   # <<<< ENSURE THIS IS PRESENT
            returns=10
        )
        
        # Login as user2 and check their accounts (should be empty of user1's trades)
        self.client.credentials() # Log out user1
        response_user2_login = self.client.post(reverse("api-token-auth"), {"username": "apiuser2", "password": "apipassword123"})
        token2 = response_user2_login.data["token"]
        self.client.credentials(HTTP_AUTHORIZATION=f"Token {token2}")

        user2_trades_url = reverse("trade-list")
        response_user2_trades = self.client.get(user2_trades_url)
        self.assertEqual(response_user2_trades.status_code, status.HTTP_200_OK)
        self.assertEqual(response_user2_trades.data['count'], 0) # User2 has no trades yet

        # Switch back to user1 for subsequent tests
        self.client.credentials(HTTP_AUTHORIZATION=f"Token {self.token1}")


    def test_trade_logging_full_create_and_retrieve_with_all_details(self): # Enhanced test name
        trade_data = {
            "account": self.account1_user1.pk, "symbol": "GOOGL", "side": "BUY",
            "entry_date": datetime.now(timezone.utc).isoformat(), "entry_price": "2500.00", "size": "1",
            "exit_date": (datetime.now(timezone.utc) + timedelta(hours=1)).isoformat(), "exit_price": "2510.00",
            "returns": "10.00", "notes": "Detailed notes here.",
            "setup_strategy": self.strategy_user1.pk, "entry_type": self.entry_type_user1.pk
        }
        create_url = reverse("trade-list")
        response_create = self.client.post(create_url, trade_data, format="json")
        self.assertEqual(response_create.status_code, status.HTTP_201_CREATED, response_create.data)
        trade_id = response_create.data['id']

        retrieve_url = reverse("trade-detail", kwargs={"pk": trade_id})
        response_retrieve = self.client.get(retrieve_url)
        self.assertEqual(response_retrieve.status_code, status.HTTP_200_OK)
        self.assertEqual(response_retrieve.data['symbol'], "GOOGL")
        self.assertEqual(response_retrieve.data['notes'], "Detailed notes here.")
        self.assertEqual(response_retrieve.data['setup_strategy'], self.strategy_user1.pk)
        self.assertEqual(response_retrieve.data['entry_type'], self.entry_type_user1.pk)
        self.assertEqual(Decimal(response_retrieve.data['returns']), Decimal("10.00"))
        self.assertEqual(Decimal(response_retrieve.data['entry_price']), Decimal("2500.00"))
        self.assertEqual(Decimal(response_retrieve.data['exit_price']), Decimal("2510.00"))
        self.assertEqual(Decimal(response_retrieve.data['size']), Decimal("1"))
        self.assertEqual(response_retrieve.data['side'], "BUY")
        self.assertIsNotNone(response_retrieve.data['duration'])
        self.assertEqual(response_retrieve.data['status'], "WIN")
        # Balance check would require more complex logic if Account.current_balance is updated by Trade save
        # self.assertEqual(Decimal(response_retrieve.data['current_balance_after_trade']), ...)


    def test_performance_dashboard_metrics_comprehensive(self): # Enhanced test name
        # Dashboard Metrics: Win/Loss Ratio, Total P&L, Average Win/Loss, Best/Worst Trades
        # "Performance by Setup Type" is not a direct endpoint yet.
        Trade.objects.create(account=self.account1_user1, symbol="A1_W1", entry_date=datetime.now(timezone.utc), exit_date=datetime.now(timezone.utc), entry_price=Decimal("10.0"), size=Decimal("1"), side="BUY", returns=100, setup_strategy=self.strategy_user1)
        Trade.objects.create(account=self.account1_user1, symbol="A1_L1", entry_date=datetime.now(timezone.utc), exit_date=datetime.now(timezone.utc), entry_price=Decimal("10.0"), size=Decimal("1"), side="BUY", returns=-50, setup_strategy=self.strategy_user1)
        Trade.objects.create(account=self.account1_user1, symbol="A1_W2", entry_date=datetime.now(timezone.utc), exit_date=datetime.now(timezone.utc), entry_price=Decimal("10.0"), size=Decimal("1"), side="BUY", returns=150, setup_strategy=self.strategy_public) # Using public strategy
        Trade.objects.create(account=self.account1_user1, symbol="A1_BE", entry_date=datetime.now(timezone.utc), exit_date=datetime.now(timezone.utc), entry_price=Decimal("10.0"), size=Decimal("1"), side="BUY", returns=0)
        Trade.objects.create(account=self.account2_user1, symbol="A2_W", entry_date=datetime.now(timezone.utc), exit_date=datetime.now(timezone.utc), entry_price=Decimal("10.0"), size=Decimal("1"), side="BUY", returns=200)

        all_metrics_url = reverse("dashboard-metrics") # For user1, all their accounts
        response_all = self.client.get(all_metrics_url)
        self.assertEqual(response_all.status_code, status.HTTP_200_OK)
        self.assertEqual(response_all.data['total_trades'], 5) # 4 for acc1, 1 for acc2
        self.assertEqual(Decimal(response_all.data['total_pnl']), Decimal("400.00")) # 100 - 50 + 150 + 0 + 200
        self.assertEqual(response_all.data['winning_trades'], 3)
        self.assertEqual(response_all.data['losing_trades'], 1)
        self.assertEqual(response_all.data['breakeven_trades'], 1)
        self.assertAlmostEqual(response_all.data['win_rate'], (3/5)*100, places=2)
        self.assertAlmostEqual(Decimal(response_all.data['average_pnl_per_trade']), Decimal("400.00")/5, places=2)
        self.assertAlmostEqual(Decimal(response_all.data['average_winning_trade']), Decimal("450.00")/3, places=2) # (100+150+200)/3
        self.assertEqual(Decimal(response_all.data['average_losing_trade']), Decimal("-50.00"))
        self.assertAlmostEqual(Decimal(response_all.data['profit_factor']), Decimal("450.00")/50, places=2) # Gross profit / Gross loss
        self.assertEqual(Decimal(response_all.data['largest_winning_trade']), Decimal("200.00"))
        self.assertEqual(Decimal(response_all.data['largest_losing_trade']), Decimal("-50.00"))


    def test_pnl_over_time_chart_data_trends(self): # Enhanced test name
        now = datetime.now(timezone.utc)
        Trade.objects.create(account=self.account1_user1, symbol="T1", entry_date=now-timedelta(days=35), exit_date=now-timedelta(days=32), entry_price=Decimal("10.0"), size=Decimal("1"), side="BUY", returns=50)
        Trade.objects.create(account=self.account1_user1, symbol="T2", entry_date=now-timedelta(days=5), exit_date=now-timedelta(days=2), entry_price=Decimal("10.0"), size=Decimal("1"), side="BUY", returns=100)
        Trade.objects.create(account=self.account1_user1, symbol="T3", entry_date=now-timedelta(days=4), exit_date=now-timedelta(days=1), entry_price=Decimal("10.0"), size=Decimal("1"), side="BUY", returns=-20)

        # Monthly
        url_monthly = reverse("pnl-over-time-data") + f"?account_id={self.account1_user1.pk}&period=monthly"
        response_monthly = self.client.get(url_monthly)
        self.assertEqual(response_monthly.status_code, status.HTTP_200_OK)
        self.assertTrue(len(response_monthly.data) >= 1)
        # Check if P&L sums correctly for the periods
        # Example: if T1 is last month, T2+T3 this month
        pnl_this_month_data = next((item for item in response_monthly.data if date.fromisoformat(item['period']).month == now.month), None)
        if pnl_this_month_data:
            self.assertEqual(Decimal(pnl_this_month_data['pnl']), Decimal("80.00")) # 100 - 20

        # Daily
        url_daily = reverse("pnl-over-time-data") + f"?account_id={self.account1_user1.pk}&period=daily"
        response_daily = self.client.get(url_daily)
        self.assertEqual(response_daily.status_code, status.HTTP_200_OK)
        self.assertEqual(len(response_daily.data), 3) # 3 distinct exit days
        pnl_sum_daily = sum(Decimal(d['pnl']) for d in response_daily.data)
        self.assertEqual(pnl_sum_daily, Decimal("130.00")) # 50 + 100 - 20


    def test_calendar_view_data_color_coded_and_summaries(self): # Enhanced test name
        year, month = 2023, 11 # November
        # Day 1: Win
        Trade.objects.create(account=self.account1_user1, symbol="NOV1", entry_date=datetime(year,month,1,9,0, tzinfo=timezone.utc), exit_date=datetime(year,month,1,10,0, tzinfo=timezone.utc), entry_price=Decimal("1.0"), size=Decimal("1"), side="BUY", returns=100, setup_strategy=self.strategy_user1)
        # Day 2: Loss and Breakeven -> Net Loss
        Trade.objects.create(account=self.account1_user1, symbol="NOV2L", entry_date=datetime(year,month,2,9,0, tzinfo=timezone.utc), exit_date=datetime(year,month,2,10,0, tzinfo=timezone.utc), entry_price=Decimal("1.0"), size=Decimal("1"), side="BUY", returns=-50, setup_strategy=self.strategy_user1)
        Trade.objects.create(account=self.account1_user1, symbol="NOV2B", entry_date=datetime(year,month,2,11,0, tzinfo=timezone.utc), exit_date=datetime(year,month,2,12,0, tzinfo=timezone.utc), entry_price=Decimal("1.0"), size=Decimal("1"), side="BUY", returns=0)
        # Day 3: No trades for this account
        # Day 4: Breakeven day
        Trade.objects.create(account=self.account1_user1, symbol="NOV4B1", entry_date=datetime(year,month,4,9,0, tzinfo=timezone.utc), exit_date=datetime(year,month,4,10,0, tzinfo=timezone.utc), returns=20)
        Trade.objects.create(account=self.account1_user1, symbol="NOV4B2", entry_date=datetime(year,month,4,11,0, tzinfo=timezone.utc), exit_date=datetime(year,month,4,12,0, tzinfo=timezone.utc), returns=-20)


        url = reverse("calendar-data") + f"?year={year}&month={month}&account_id={self.account1_user1.pk}"
        response = self.client.get(url)
        self.assertEqual(response.status_code, status.HTTP_200_OK, response.data)
        
        self.assertEqual(response.data['year'], year)
        self.assertEqual(response.data['month'], month)
        self.assertEqual(Decimal(response.data['total_monthly_pnl']), Decimal("50.00")) # 100 - 50 + 0 + 20 - 20
        self.assertEqual(response.data['total_monthly_trades'], 5) # Daily trade count & P&L summaries (monthly total)
        self.assertEqual(response.data['winning_days'], 1) # Color-coded monthly performance (summary)
        self.assertEqual(response.data['losing_days'], 1)  # Day 2
        self.assertEqual(response.data['breakeven_days'], 1) # Day 4
        
        days_with_trades = response.data['days_with_trades']
        self.assertEqual(len(days_with_trades), 3) # Days 1, 2, 4 had trades

        day1_data = next(d for d in days_with_trades if d['date'] == f"{year}-{month:02d}-01")
        self.assertEqual(day1_data['day_status'], "WINNING_DAY") # Color-coded (implicitly via status)
        self.assertEqual(Decimal(day1_data['total_pnl']), Decimal("100.00")) # Daily P&L summary
        self.assertEqual(day1_data['trade_count'], 1) # Daily trade count summary
        self.assertIn(self.strategy_user1.name, day1_data['strategies_used']) # Hover or click to view daily trades (strategies)

        day2_data = next(d for d in days_with_trades if d['date'] == f"{year}-{month:02d}-02")
        self.assertEqual(day2_data['day_status'], "LOSING_DAY")
        self.assertEqual(Decimal(day2_data['total_pnl']), Decimal("-50.00"))
        
        day4_data = next(d for d in days_with_trades if d['date'] == f"{year}-{month:02d}-04")
        self.assertEqual(day4_data['day_status'], "BREAKEVEN_DAY")
        self.assertEqual(Decimal(day4_data['total_pnl']), Decimal("0.00"))


    def test_setup_entry_type_management_and_analysis_placeholders(self): # Renamed
        # Test for "Customize and manage trade setups" & "Note entry strategies with images"
        # Current tests cover CRUD for SetupStrategy.
        # "Analyze setups by performance" is not a direct endpoint yet.
        # This would require a new view that aggregates Trade.returns grouped by SetupStrategy.
        
        # List (user1 sees their own + public)
        list_url = reverse("setupstrategy-list")
        response = self.client.get(list_url)
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        # Paginated response, check names
        names = [s['name'] for s in response.data['results']]
        self.assertIn(self.strategy_user1.name, names)
        self.assertIn(self.strategy_public.name, names)
        self.assertEqual(len(names), 2)


        # Create (tests "Customize and manage trade setups")
        create_data = {"name": "User1 New Strat", "description": "A new one for user1 with detailed notes."} # Test "Strategy documentation"
        response_create = self.client.post(list_url, create_data, format="json")
        self.assertEqual(response_create.status_code, status.HTTP_201_CREATED, response_create.data)
        self.assertEqual(response_create.data['name'], "User1 New Strat")
        new_strat_id = response_create.data['id']

        # Retrieve (user1 can retrieve their own)
        detail_url = reverse("setupstrategy-detail", kwargs={"pk": new_strat_id})
        response_retrieve = self.client.get(detail_url)
        self.assertEqual(response_retrieve.status_code, status.HTTP_200_OK)

        # Update (user1 can update their own)
        update_data = {"name": "User1 Updated Strat", "description": "Updated desc"}
        response_update = self.client.put(detail_url, update_data, format="json")
        self.assertEqual(response_update.status_code, status.HTTP_200_OK)
        self.assertEqual(response_update.data['name'], "User1 Updated Strat")

        # User1 tries to update public strategy (should fail if not allowed, depends on IsOwnerOrReadOnly logic for user=None)
        public_strat_detail_url = reverse("setupstrategy-detail", kwargs={"pk": self.strategy_public.pk})
        response_update_public = self.client.put(public_strat_detail_url, {"name": "Attempt Update Public"}, format="json")
        self.assertEqual(response_update_public.status_code, status.HTTP_403_FORBIDDEN)


        # User2 (unauthenticated for this client) cannot modify user1's strategy
        self.client.credentials() # Log out user1
        response_unauth_update = self.client.put(detail_url, update_data, format="json")
        self.assertEqual(response_unauth_update.status_code, status.HTTP_403_FORBIDDEN) # Changed to 403

        # Login user2
        response_user2_login = self.client.post(reverse("api-token-auth"), {"username": "apiuser2", "password": "apipassword123"})
        token2 = response_user2_login.data["token"]
        self.client.credentials(HTTP_AUTHORIZATION=f"Token {token2}")
        
        response_user2_update_user1_strat = self.client.put(detail_url, update_data, format="json")
        self.assertEqual(response_user2_update_user1_strat.status_code, status.HTTP_403_FORBIDDEN)

        # Delete (user1 can delete their own)
        self.client.credentials(HTTP_AUTHORIZATION=f"Token {self.token1}") # Login user1 again
        response_delete = self.client.delete(detail_url)
        self.assertEqual(response_delete.status_code, status.HTTP_204_NO_CONTENT)


    def test_trade_journal_notes_and_screenshots_api(self):
        # Test for "Add rich notes per trade" and "Upload screenshots/charts"
        # Notes are tested in test_trade_logging_full_create_and_retrieve_with_all_details

        # Screenshot upload test
        trade = Trade.objects.create(
            account=self.account1_user1, symbol="IMG_TRADE",
            entry_date=datetime.now(timezone.utc),
            entry_price=Decimal("100.00"), size=Decimal("1"), side="BUY" # Corrected
        )
        
        image_content = b'GIF89a\x01\x00\x01\x00\x80\x00\x00\x00\x00\x00\xff\xff\xff!\xf9\x04\x01\x00\x00\x00\x00,\x00\x00\x00\x00\x01\x00\x01\x00\x00\x02\x02D\x01\x00;'
        image_file = SimpleUploadedFile("test_screenshot.gif", image_content, content_type="image/gif")
        
        screenshot_url = reverse("screenshot-list")
        screenshot_data = {
            "trade_id": trade.pk,
            "caption": "Entry point screenshot",
            "image": image_file
        }
        response = self.client.post(screenshot_url, screenshot_data, format="multipart")
        # Line 491:
        self.assertEqual(response.status_code, status.HTTP_201_CREATED, response.data)
        self.assertIn("image", response.data)
        self.assertTrue(response.data["image"].endswith("test_screenshot.gif"))
        self.assertEqual(response.data["caption"], "Entry point screenshot")
        
        self.assertEqual(Screenshot.objects.filter(trade=trade).count(), 1)


# Placeholder for features not yet implemented in backend:
# class FutureFeatureTests(APITestCase):
#     def test_custom_risk_parameters_on_account(self):
#         # Would require Account model to have risk fields (e.g., max_risk_per_trade)
#         # And API to set/get them.
#         pass

#     def test_trading_rules_crud(self):
#         # Would require a TradingRule model and associated API endpoints.
#         pass

#     def test_symbol_management_api(self):
#         # Would require a Symbol model (e.g., for pre-defined symbols, asset classes)
#         # And API endpoints to manage them.
#         pass

#     def test_csv_import_export_trades(self):
#         # Would require dedicated API endpoints for file upload (CSV) and download.
#         pass

#     def test_backup_restore_functionality(self):
#         # Complex feature, likely involving management commands or dedicated API endpoints
#         # for creating and restoring database snapshots or serialized data.
#         pass