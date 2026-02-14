from rest_framework import serializers
from .models import Account, Trade, SetupStrategy, EntryType, SetupStrategyImage, EntryTypeImage
from .documentation import DocumentationWidget, DocumentationItem
from django.contrib.auth.models import User
from django.contrib.auth.password_validation import validate_password # For password validation
from rest_framework.authtoken.models import Token # Import Token model

# Serializer for DocumentationItem model
class DocumentationItemSerializer(serializers.ModelSerializer):
    class Meta:
        model = DocumentationItem
        fields = ['id', 'item_type', 'text_content', 'image', 'order']

# Serializer for DocumentationWidget model
class DocumentationWidgetSerializer(serializers.ModelSerializer):
    items = DocumentationItemSerializer(many=True, read_only=True)
    
    class Meta:
        model = DocumentationWidget
        fields = ['id', 'order', 'items', 'created_at', 'updated_at']

# Legacy compatibility serializer for DocumentationBlock
class DocumentationBlockSerializer(serializers.Serializer):
    id = serializers.IntegerField(read_only=True)
    block_type = serializers.CharField()
    text_content = serializers.CharField(allow_null=True, required=False)
    image_content = serializers.FileField(allow_null=True, required=False)
    order = serializers.IntegerField(default=0)


class UserSerializer(serializers.ModelSerializer):
    """
    Serializer for the User model.
    """
    class Meta:
        model = User
        fields = ['id', 'username', 'email', 'first_name', 'last_name'] # Add other fields as needed
        # Make email readable for authenticated users, but not necessarily writable here
        # Writable fields for user update can be handled in a separate serializer or view logic


class UserRegistrationSerializer(serializers.ModelSerializer):
    password = serializers.CharField(write_only=True, required=True, validators=[validate_password])
    password2 = serializers.CharField(write_only=True, required=True, label="Confirm password")
    email = serializers.EmailField(required=True)

    class Meta:
        model = User
        fields = ('username', 'email', 'password', 'password2', 'first_name', 'last_name')
        extra_kwargs = {
            'first_name': {'required': False},
            'last_name': {'required': False}
        }

    def validate_email(self, value):
        if User.objects.filter(email=value).exists():
            raise serializers.ValidationError("A user with that email already exists.")
        return value

    def validate(self, attrs):
        if attrs['password'] != attrs['password2']:
            raise serializers.ValidationError({"password": "Password fields didn't match."}) # Or non_field_errors
        return attrs

    def create(self, validated_data):
        user = User.objects.create_user(
            username=validated_data['username'],
            email=validated_data['email'],
            first_name=validated_data.get('first_name', ''),
            last_name=validated_data.get('last_name', '')
        )
        user.set_password(validated_data['password'])
        user.save()
        Token.objects.create(user=user) 
        return user

class AccountSerializer(serializers.ModelSerializer):
    user = serializers.PrimaryKeyRelatedField(read_only=True) # User is read-only, set in the view

    class Meta:
        model = Account
        fields = ['id', 'user', 'name', 'initial_balance', 'current_balance', 'created_at', 'updated_at']
        read_only_fields = ['user', 'created_at', 'updated_at']
        extra_kwargs = {
            'current_balance': {'required': False}
        }

    def create(self, validated_data):
        # On creation, current_balance is set to initial_balance
        validated_data['current_balance'] = validated_data.get('initial_balance')
        return super().create(validated_data)

class SetupStrategyImageSerializer(serializers.ModelSerializer):
    class Meta:
        model = SetupStrategyImage
        fields = ['id', 'image']

class EntryTypeImageSerializer(serializers.ModelSerializer):
    class Meta:
        model = EntryTypeImage
        fields = ['id', 'image']

class SetupStrategySerializer(serializers.ModelSerializer):
    documentationBlocks = serializers.SerializerMethodField()
    documentation_blocks_input = serializers.CharField(write_only=True, required=False)

    class Meta:
        model = SetupStrategy
        fields = ['id', 'user', 'name', 'description', 'documentationBlocks', 'documentation_blocks_input']
        read_only_fields = ['user', 'documentationBlocks']

    def create(self, validated_data):
        documentation_blocks_json = validated_data.pop('documentation_blocks', '[]')
        instance = super().create(validated_data)
        self._save_documentation_blocks(instance, documentation_blocks_json)
        return instance

    def get_documentationBlocks(self, obj):
        from django.contrib.contenttypes.models import ContentType
        
        content_type = ContentType.objects.get_for_model(obj)
        widgets = DocumentationWidget.objects.filter(
            content_type=content_type,
            object_id=obj.id
        )
        
        documentation_blocks = []
        for widget in widgets:
            for item in widget.items.all():
                block = {
                    'id': item.id,
                    'block_type': item.item_type.lower(),
                    'text_content': item.text_content if item.item_type == 'TEXT' else None,
                    'image_content': item.image.url if item.image and item.item_type == 'IMAGE' else None,
                    'order': item.order
                }
                documentation_blocks.append(block)
        
        return sorted(documentation_blocks, key=lambda x: x['order'])

    def update(self, instance, validated_data):
        documentation_blocks_json = validated_data.pop('documentation_blocks', None)
        instance = super().update(instance, validated_data)
        if documentation_blocks_json is not None:
            self._save_documentation_blocks(instance, documentation_blocks_json, update=True)
        return instance

    def _save_documentation_blocks(self, instance, documentation_blocks_json, update=False):
        import json
        from .models import DocumentationBlock
        request = self.context.get('request')
        files = getattr(request, 'FILES', {}) if request else {}
        try:
            blocks = json.loads(documentation_blocks_json)
        except Exception:
            blocks = []
        if update:
            instance.documentation_blocks.all().delete()
        for i, block in enumerate(blocks):
            image_value = block.get('image')
            if block.get('type') == 'image':
                # If image_value is a key (e.g. 'image_0'), get the file from request.FILES
                if image_value and isinstance(image_value, str) and image_value in files:
                    image_value = files[image_value]
                elif not image_value or image_value == {}:
                    image_value = None
            DocumentationBlock.objects.create(
                content_object=instance,
                block_type=block.get('type'),
                text_content=block.get('text'),
                image_content=image_value,
                order=i
            )

class EntryTypeSerializer(serializers.ModelSerializer):
    documentationBlocks = serializers.SerializerMethodField()
    documentation_blocks_input = serializers.CharField(write_only=True, required=False)

    class Meta:
        model = EntryType
        fields = ['id', 'user', 'name', 'description', 'documentationBlocks', 'documentation_blocks_input']
        read_only_fields = ['user', 'documentationBlocks']

    def create(self, validated_data):
        documentation_blocks_json = validated_data.pop('documentation_blocks', '[]')
        instance = super().create(validated_data)
        self._save_documentation_blocks(instance, documentation_blocks_json)
        return instance

    def get_documentationBlocks(self, obj):
        from django.contrib.contenttypes.models import ContentType
        
        content_type = ContentType.objects.get_for_model(obj)
        widgets = DocumentationWidget.objects.filter(
            content_type=content_type,
            object_id=obj.id
        )
        
        documentation_blocks = []
        for widget in widgets:
            for item in widget.items.all():
                block = {
                    'id': item.id,
                    'block_type': item.item_type.lower(),
                    'text_content': item.text_content if item.item_type == 'TEXT' else None,
                    'image_content': item.image.url if item.image and item.item_type == 'IMAGE' else None,
                    'order': item.order
                }
                documentation_blocks.append(block)
        
        return sorted(documentation_blocks, key=lambda x: x['order'])

    def update(self, instance, validated_data):
        documentation_blocks_json = validated_data.pop('documentation_blocks', None)
        instance = super().update(instance, validated_data)
        if documentation_blocks_json is not None:
            self._save_documentation_blocks(instance, documentation_blocks_json, update=True)
        return instance

    def _save_documentation_blocks(self, instance, documentation_blocks_json, update=False):
        import json
        from .models import DocumentationBlock
        request = self.context.get('request')
        files = getattr(request, 'FILES', {}) if request else {}
        try:
            blocks = json.loads(documentation_blocks_json)
        except Exception:
            blocks = []
        if update:
            instance.documentation_blocks.all().delete()
        for i, block in enumerate(blocks):
            image_value = block.get('image')
            if block.get('type') == 'image':
                # If image_value is a key (e.g. 'image_0'), get the file from request.FILES
                if image_value and isinstance(image_value, str) and image_value in files:
                    image_value = files[image_value]
                elif not image_value or image_value == {}:
                    image_value = None
            DocumentationBlock.objects.create(
                content_object=instance,
                block_type=block.get('type'),
                text_content=block.get('text'),
                image_content=image_value,
                order=i
            )

class TradeSerializer(serializers.ModelSerializer):
    status = serializers.CharField(read_only=True)
    documentationBlocks = serializers.SerializerMethodField()
    documentation_blocks = serializers.CharField(write_only=True, required=False)

    class Meta:
        model = Trade
        fields = [
            'id', 'account', 'symbol', 'entry_date', 'entry_price', 'exit_price', 'size', 'side',
            'duration', 'returns', 'current_balance_after_trade', 'notes',
            'setup_strategy', 'entry_type', 'status', 'created_at', 'updated_at',
            'documentationBlocks', 'documentation_blocks'
        ]
        read_only_fields = ['status', 'created_at', 'updated_at', 'current_balance_after_trade', 'documentationBlocks']
        
    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        # Make all fields optional for PUT/PATCH requests
        request = self.context.get('request')
        if request and request.method in ['PUT', 'PATCH']:
            for field in self.fields:
                if field not in self.Meta.read_only_fields:
                    self.fields[field].required = False

    def create(self, validated_data):
        documentation_blocks_json = validated_data.pop('documentation_blocks', '[]')
        instance = super().create(validated_data)
        self._save_documentation_blocks(instance, documentation_blocks_json)
        return instance

    def get_documentationBlocks(self, obj):
        from django.contrib.contenttypes.models import ContentType
        
        content_type = ContentType.objects.get_for_model(obj)
        widgets = DocumentationWidget.objects.filter(
            content_type=content_type,
            object_id=obj.id
        )
        
        documentation_blocks = []
        for widget in widgets:
            for item in widget.items.all():
                block = {
                    'id': item.id,
                    'block_type': item.item_type.lower(),
                    'text_content': item.text_content if item.item_type == 'TEXT' else None,
                    'image_content': item.image.url if item.image and item.item_type == 'IMAGE' else None,
                    'order': item.order
                }
                documentation_blocks.append(block)
        
        return sorted(documentation_blocks, key=lambda x: x['order'])

    def update(self, instance, validated_data):
        documentation_blocks_json = validated_data.pop('documentation_blocks', None)
        instance = super().update(instance, validated_data)
        if documentation_blocks_json is not None:
            self._save_documentation_blocks(instance, documentation_blocks_json, update=True)
        return instance

    def _save_documentation_blocks(self, instance, documentation_blocks_json, update=False):
        import json
        from .models import DocumentationBlock
        request = self.context.get('request')
        files = getattr(request, 'FILES', {}) if request else {}
        try:
            blocks = json.loads(documentation_blocks_json)
        except Exception:
            blocks = []
        if update:
            instance.documentation_blocks.all().delete()
        for i, block in enumerate(blocks):
            image_value = block.get('image')
            if block.get('type') == 'image':
                if image_value and isinstance(image_value, str) and image_value in files:
                    image_value = files[image_value]
                elif not image_value or image_value == {}:
                    image_value = None
            DocumentationBlock.objects.create(
                content_object=instance,
                block_type=block.get('type'),
                text_content=block.get('text'),
                image_content=image_value,
                order=i
            )

# --- New Serializers for Metrics ---

class DashboardMetricsSerializer(serializers.Serializer):
    total_pnl = serializers.DecimalField(max_digits=12, decimal_places=2)
    total_trades = serializers.IntegerField()
    winning_trades = serializers.IntegerField()
    losing_trades = serializers.IntegerField()
    breakeven_trades = serializers.IntegerField()
    win_rate = serializers.FloatField() # Percentage, e.g., 0.75 for 75%
    average_pnl_per_trade = serializers.DecimalField(max_digits=12, decimal_places=2)
    average_winning_trade = serializers.DecimalField(max_digits=12, decimal_places=2, allow_null=True)
    average_losing_trade = serializers.DecimalField(max_digits=12, decimal_places=2, allow_null=True)
    profit_factor = serializers.FloatField(allow_null=True) # Gross Profit / Gross Loss
    largest_winning_trade = serializers.DecimalField(max_digits=12, decimal_places=2, allow_null=True)
    largest_losing_trade = serializers.DecimalField(max_digits=12, decimal_places=2, allow_null=True)
    account_name = serializers.CharField(required=False, allow_null=True) # Optional: if metrics are per account
    account_id = serializers.IntegerField(required=False, allow_null=True)


class EquityDataPointSerializer(serializers.Serializer):
    date = serializers.DateTimeField()
    balance = serializers.DecimalField(max_digits=12, decimal_places=2)


class PnlOverTimeDataPointSerializer(serializers.Serializer):
    period = serializers.DateField() # Could be CharField if period is like "Jan 2023"
    pnl = serializers.DecimalField(max_digits=12, decimal_places=2)


# --- New Serializers for Calendar View ---

class DailyCalendarEntrySerializer(serializers.Serializer):
    date = serializers.DateField()
    total_pnl = serializers.DecimalField(max_digits=12, decimal_places=2)
    trade_count = serializers.IntegerField()
    strategies_used = serializers.ListField(child=serializers.CharField())
    day_status = serializers.CharField() # e.g., "WINNING_DAY", "LOSING_DAY", "BREAKEVEN_DAY", "NO_TRADES"

class MonthlyCalendarSerializer(serializers.Serializer):
    year = serializers.IntegerField()
    month = serializers.IntegerField()
    account_id = serializers.IntegerField(required=False, allow_null=True)
    account_name = serializers.CharField(required=False, allow_null=True)
    
    total_monthly_pnl = serializers.DecimalField(max_digits=12, decimal_places=2)
    total_monthly_trades = serializers.IntegerField()
    winning_days = serializers.IntegerField()
    losing_days = serializers.IntegerField()
    breakeven_days = serializers.IntegerField()
    
    days_with_trades = serializers.ListField(child=DailyCalendarEntrySerializer())
