from django.db import models
from django.contrib.auth.models import User
from django.core.validators import MinValueValidator
from django.db.models.signals import post_save, post_delete, pre_save
from django.dispatch import receiver
from decimal import Decimal
from django.contrib.contenttypes.fields import GenericForeignKey
from django.contrib.contenttypes.models import ContentType
from .documentation import DocumentationWidget, DocumentationItem

# Create your models here. # Modellerinizi burada oluşturun.

class Account(models.Model): #
    user = models.ForeignKey(User, on_delete=models.CASCADE, related_name='accounts') # Hesabın sahibi olan kullanıcı. User silindiğinde ilişkili hesaplar da silinir (CASCADE).
    name = models.CharField(max_length=100) 
    initial_balance = models.DecimalField(max_digits=12, decimal_places=2, validators=[MinValueValidator(0)]) 
    current_balance = models.DecimalField(max_digits=12, decimal_places=2) # Mevcut bakiye. Her işlemden sonra güncellenmeli.
    created_at = models.DateTimeField(auto_now_add=True) # Hesabın oluşturulma tarihi, otomatik olarak ayarlanır.
    updated_at = models.DateTimeField(auto_now=True) # Hesabın son güncellenme tarihi, otomatik olarak ayarlanır.

    def __str__(self): # Modelin string temsilini döndürür.
        return f"{self.name} ({self.user.username})"

    class Meta: 
        ordering = ['name'] 

from django.contrib.contenttypes.fields import GenericRelation

class SetupStrategy(models.Model): # İşlem setup stratejisi modelini temsil eder.
    user = models.ForeignKey(User, on_delete=models.CASCADE, related_name='setup_strategies', null=True, blank=True) # Stratejinin sahibi olan kullanıcı (isteğe bağlı).
    name = models.CharField(max_length=100, unique=True) # Stratejinin adı, benzersiz olmalı (kullanıcı başına mı yoksa global mi düşünülmeli).
    description = models.TextField(blank=True, null=True) # Strateji ile ilgili açıklama veya notlar (isteğe bağlı, yeniden eklendi).

    def __str__(self): # Modelin string temsilini döndürür.
        return self.name

    class Meta: # Model için meta seçenekleri.
        verbose_name_plural = "Setup Strategies" # Admin panelinde çoğul adı.
        ordering = ['name'] # Varsayılan sıralama: isme göre.

class EntryType(models.Model): # Giriş tipi modelini temsil eder.
    user = models.ForeignKey(User, on_delete=models.CASCADE, related_name='entry_types', null=True, blank=True) # Giriş tipinin sahibi olan kullanıcı (isteğe bağlı).
    name = models.CharField(max_length=100, unique=True) # Giriş tipinin adı, benzersiz olmalı (kullanıcı başına mı yoksa global mi düşünülmeli).
    description = models.TextField(blank=True, null=True) # Giriş tipi ile ilgili açıklama veya notlar (isteğe bağlı, yeniden eklendi).

    def __str__(self): # Modelin string temsilini döndürür.
        return self.name

    class Meta: # Model için meta seçenekleri.
        verbose_name_plural = "Entry Types" # Admin panelinde çoğul adı.
        ordering = ['name'] # Varsayılan sıralama: isme göre.

class SetupStrategyImage(models.Model):
    strategy = models.ForeignKey(SetupStrategy, related_name='images', on_delete=models.CASCADE)
    image = models.ImageField(upload_to='setup_strategy_images/')

    def __str__(self):
        return f"Image for {self.strategy.name}"

class EntryTypeImage(models.Model):
    entry_type = models.ForeignKey(EntryType, related_name='images', on_delete=models.CASCADE)
    image = models.ImageField(upload_to='entry_type_images/')

    def __str__(self):
        return f"Image for {self.entry_type.name}"

class Trade(models.Model): # Bir alım satım işlemini temsil eder.
    SIDE_CHOICES = [ # İşlem tarafı için seçenekler.
        ('BUY', 'Buy'), # Alış
        ('SELL', 'Sell'), # Satış
    ]

    account = models.ForeignKey(Account, on_delete=models.CASCADE, related_name='trades') # İşlemin yapıldığı hesap. Hesap silindiğinde ilişkili işlemler de silinir.
    symbol = models.CharField(max_length=20) # İşlem yapılan sembol (örneğin, AAPL, BTCUSD).
    
    entry_date = models.DateTimeField() # İşleme giriş tarihi ve saati.
    
    entry_price = models.DecimalField(max_digits=12, decimal_places=5) # Giriş fiyatı (kripto/forex için artırılmış hassasiyet).
    exit_price = models.DecimalField(max_digits=12, decimal_places=5, blank=True, null=True) # Çıkış fiyatı (isteğe bağlı).
    
    size = models.DecimalField(max_digits=12, decimal_places=5) # İşlem büyüklüğü (örneğin, hisse sayısı, kontrat sayısı).
    side = models.CharField(max_length=4, choices=SIDE_CHOICES) # İşlem tarafı (Alış/Satış).
    
    duration = models.CharField(max_length=100, blank=True, null=True) # İşlem süresi, manuel giriş.
    
    # 'returns' P&L (Kar ve Zarar) anlamına gelir ve artık manuel olarak girilir.
    returns = models.DecimalField(max_digits=12, decimal_places=2, blank=True, null=True) # Bu işlem için Kar/Zarar.
    
    # Bu alan, Account.current_balance korunuyorsa gereksiz olabilir.
    # Burada saklamak, işlem kapatıldığı andaki bir anlık görüntüdür.
    current_balance_after_trade = models.DecimalField(max_digits=12, decimal_places=2, blank=True, null=True) # İşlem sonrası mevcut bakiye.
    
    notes = models.TextField(blank=True, null=True) # İşlem hakkında notlar.
    setup_strategy = models.ForeignKey(SetupStrategy, on_delete=models.SET_NULL, blank=True, null=True, related_name='trades') # Bu işlem için kullanılan strateji (isteğe bağlı).
    entry_type = models.ForeignKey(EntryType, on_delete=models.SET_NULL, blank=True, null=True, related_name='trades') # Bu işlem için giriş tipi (isteğe bağlı).

    created_at = models.DateTimeField(auto_now_add=True) # İşlemin oluşturulma tarihi, otomatik olarak ayarlanır.
    updated_at = models.DateTimeField(auto_now=True) # İşlemin son güncellenme tarihi, otomatik olarak ayarlanır.

    @property # Bir metodu özellik gibi çağırmayı sağlar.
    def status(self): # İşlemin durumunu döndürür.
        if self.returns is None: # Eğer P&L girilmemişse işlem açık kabul edilir.
            return "OPEN"
        if self.returns > 0:
            return "WIN" # Kazanç
        elif self.returns < 0:
            return "LOSS" # Kayıp
        else:
            return "BREAKEVEN" # Başa baş

    def __str__(self): # Modelin string temsilini döndürür.
        return f"{self.side} {self.symbol} @ {self.entry_price} on {self.entry_date.strftime('%Y-%m-%d')}"

    def save(self, *args, **kwargs): # Model kaydedildiğinde çağrılır.
        # Süre artık manuel olarak girildiği için hesaplama kaldırıldı.
        super().save(*args, **kwargs) # Üst sınıfın save metodunu çağır.

    class Meta: # Model için meta seçenekleri.
        ordering = ['-entry_date'] # Varsayılan sıralama: giriş tarihine göre azalan sırada.

# Signal to store the old P&L before a trade is updated
@receiver(pre_save, sender=Trade)
def store_old_returns(sender, instance, **kwargs):
    if instance.pk:
        try:
            instance._old_returns = Trade.objects.get(pk=instance.pk).returns or Decimal('0.00')
        except Trade.DoesNotExist:
            instance._old_returns = Decimal('0.00')
    else:
        instance._old_returns = Decimal('0.00')

# Signal to update account balance after a trade is saved (created or updated)
@receiver(post_save, sender=Trade)
def update_balance_on_trade_save(sender, instance, created, **kwargs):
    account = instance.account
    new_returns = instance.returns or Decimal('0.00')
    
    if created:
        # For new trades, just add the P&L
        pnl_change = new_returns
    else:
        # For updated trades, calculate the difference
        old_returns = instance._old_returns
        pnl_change = new_returns - old_returns
        
    if pnl_change != Decimal('0.00'):
        account.current_balance += pnl_change
        account.save(update_fields=['current_balance'])

# Signal to update account balance after a trade is deleted
@receiver(post_delete, sender=Trade)
def update_balance_on_trade_delete(sender, instance, **kwargs):
    account = instance.account
    returns_to_revert = instance.returns or Decimal('0.00')
    
    if returns_to_revert != Decimal('0.00'):
        account.current_balance -= returns_to_revert
        account.save(update_fields=['current_balance'])
