from rest_framework import serializers
from .models import Account, Trade, SetupStrategy, EntryType, SetupStrategyImage, EntryTypeImage
from .documentation import DocumentationWidget, DocumentationItem
from django.contrib.auth.models import User
from django.contrib.auth.password_validation import validate_password # For password validation
from rest_framework.authtoken.models import Token # Import Token model
from django.contrib.contenttypes.models import ContentType

# Helper function for all serializers to get documentation blocks
def get_documentation_blocks_for_object(obj):
    """
    Helper function to get documentation blocks for any object with a GenericForeignKey
    """
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
                'block_type': item.item_type.lower(),  # Convert 'TEXT'/'IMAGE' to 'text'/'image'
                'text_content': item.text_content if item.item_type == 'TEXT' else None,
                'image_content': item.image.url if item.image and item.item_type == 'IMAGE' else None,
                'order': item.order
            }
            documentation_blocks.append(block)
    
    return sorted(documentation_blocks, key=lambda x: x['order'])
