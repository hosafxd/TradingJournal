from rest_framework import serializers
from .documentation import DocumentationWidget, DocumentationItem
from django.contrib.contenttypes.models import ContentType

class DocumentationItemSerializer(serializers.ModelSerializer):
    class Meta:
        model = DocumentationItem
        fields = ['id', 'widget', 'item_type', 'text_content', 'image', 'order']
        read_only_fields = ['id']
        
class DocumentationWidgetSerializer(serializers.ModelSerializer):
    items = DocumentationItemSerializer(many=True, read_only=True)
    parent_type = serializers.CharField(write_only=True, required=False)
    parent_id = serializers.IntegerField(write_only=True, required=False)
    
    class Meta:
        model = DocumentationWidget
        fields = ['id', 'content_type', 'object_id', 'order', 'created_at', 'updated_at', 'items', 'parent_type', 'parent_id']
        read_only_fields = ['id', 'created_at', 'updated_at', 'content_type', 'object_id']
        
    def create(self, validated_data):
        parent_type = validated_data.pop('parent_type', None)
        parent_id = validated_data.pop('parent_id', None)
        
        if parent_type and parent_id:
            model_map = {
                'trade': 'trade',
                'setupstrategy': 'setupstrategy',
                'entrytype': 'entrytype'
            }
            
            model_name = model_map.get(parent_type.lower())
            if model_name:
                content_type = ContentType.objects.get(model=model_name)
                validated_data['content_type'] = content_type
                validated_data['object_id'] = parent_id
        
        return super().create(validated_data)
