import django_filters
from .models import Trade

class TradeFilter(django_filters.FilterSet):
    # Explicitly define a filter for the 'account' field.
    # This will allow filtering by account ID, e.g., /api/trades/?account=1
    # We can also add filters for other fields if needed.
    # For example, to filter by symbol:
    # symbol = django_filters.CharFilter(lookup_expr='iexact')

    class Meta:
        model = Trade
        # Define fields that can be filtered.
        # 'account' will filter by the Account's primary key.
        fields = ['account', 'symbol', 'side', 'setup_strategy', 'entry_type']
