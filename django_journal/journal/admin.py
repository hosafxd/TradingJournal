from django.contrib import admin
from .models import Account, Trade, SetupStrategy, EntryType
from .documentation import DocumentationWidget, DocumentationItem

# Django admin paneli, uygulamanızdaki verileri yönetmek için güçlü bir arayüzdür.
# Bu dosyada, modellerinizin admin panelinde nasıl görüneceğini ve davranacağını yapılandırırsınız.
# Kullanıcıların doğrudan girdiği verileri değil, adminlerin bu verilere nasıl erişip
# yöneteceğini tanımlarsınız.

# Temel kayıt (Basic registration)
# Bu, modelleri admin paneline en basit şekilde ekler.
# Django, model alanlarına göre varsayılan bir arayüz oluşturur.

# --- DocumentationWidget Admin Configuration ---
from django.contrib.contenttypes.admin import GenericTabularInline

class DocumentationItemInline(admin.TabularInline):
    model = DocumentationItem
    extra = 1

@admin.register(DocumentationWidget)
class DocumentationWidgetAdmin(admin.ModelAdmin):
    list_display = ('id', 'content_type', 'object_id', 'order')
    inlines = [DocumentationItemInline]

@admin.register(DocumentationItem)
class DocumentationItemAdmin(admin.ModelAdmin):
    list_display = ('id', 'item_type', 'order', 'widget')

# Custom admin for SetupStrategy
@admin.register(SetupStrategy)
class SetupStrategyAdmin(admin.ModelAdmin):
    list_display = ('name', 'user')

# Custom admin for EntryType
@admin.register(EntryType)
class EntryTypeAdmin(admin.ModelAdmin):
    list_display = ('name', 'user')

admin.site.register(Account) # Account modelini admin paneline kaydet.



# Trade admin registration
@admin.register(Trade)
class TradeAdmin(admin.ModelAdmin):
    list_display = ('symbol', 'account', 'side', 'entry_date', 'entry_price', 'exit_price', 'duration', 'returns', 'setup_strategy', 'entry_type')
    list_filter = ('account', 'side', 'entry_date', 'setup_strategy', 'entry_type')
    search_fields = ('symbol', 'notes', 'account__name')
    date_hierarchy = 'entry_date'

# Eğer tüm modeller için basit kayıt yeterliyse, aşağıdaki gibi de yapılabilir:
# (If you prefer simple registration for all, you can just do:)
# admin.site.register(Trade)
# admin.site.register(Screenshot)
