from django.urls import path, include
from rest_framework.routers import DefaultRouter
from .views import (
    AccountViewSet, TradeViewSet, SetupStrategyViewSet,
    EntryTypeViewSet, UserRegistrationView,
    DashboardMetricsView, EquityCurveDataView, PnlOverTimeDataView,
    CalendarDataView, UserDetailsView,
    ScreenshotView # Import ScreenshotView
)
from .documentation_views import DocumentationWidgetViewSet, DocumentationItemViewSet
from rest_framework.authtoken.views import obtain_auth_token

# Create a router and register our viewsets with it.
router = DefaultRouter()
router.register(r'accounts', AccountViewSet, basename='account')
router.register(r'trades', TradeViewSet, basename='trade')
router.register(r'setup-strategies', SetupStrategyViewSet, basename='setupstrategy')
router.register(r'entry-types', EntryTypeViewSet, basename='entrytype')
router.register(r'documentation', DocumentationWidgetViewSet, basename='documentation')
router.register(r'documentation-items', DocumentationItemViewSet, basename='documentationitem')

# The API URLs are now determined automatically by the router.
urlpatterns = [
    path('', include(router.urls)),
    
    # Authentication specific endpoints to match frontend
    path('auth/register/', UserRegistrationView.as_view(), name='user-register'),
    path('auth/login/', obtain_auth_token, name='api-token-auth'),
    path('auth/user/', UserDetailsView.as_view(), name='user-details'),
    
    # Metrics and Chart Data Endpoints to match frontend
    path('dashboard/metrics/', DashboardMetricsView.as_view(), name='dashboard-metrics'),
    path('dashboard/equity-curve/', EquityCurveDataView.as_view(), name='equity-curve-data'),
    path('dashboard/pnl-over-time/', PnlOverTimeDataView.as_view(), name='pnl-over-time-data'),
    
    # Calendar Data Endpoint to match frontend
    path('calendar/monthly/', CalendarDataView.as_view(), name='calendar-data'),
    
    # Screenshots endpoints (using DocumentationBlock under the hood)
    path('screenshots/', ScreenshotView.as_view(), name='screenshot-create'),
    path('screenshots/<int:pk>/', ScreenshotView.as_view(), name='screenshot-detail'),
]
