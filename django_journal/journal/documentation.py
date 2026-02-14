from django.db import models
from django.contrib.contenttypes.fields import GenericForeignKey
from django.contrib.contenttypes.models import ContentType

class DocumentationWidget(models.Model):
    """A reusable documentation widget that can be attached to any model"""
    content_type = models.ForeignKey(ContentType, on_delete=models.CASCADE)
    object_id = models.PositiveIntegerField()
    content_object = GenericForeignKey('content_type', 'object_id')
    
    # For ordering multiple widgets if needed
    order = models.PositiveIntegerField(default=0)
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)
    
    class Meta:
        ordering = ['order', 'created_at']
        
    def __str__(self):
        return f"Documentation for {self.content_object}"

class DocumentationItem(models.Model):
    """MULTIPART PARSER ILE DOCUMENT TYPE BLOCKS"""
    ITEM_TYPES = [
        ('TEXT', 'Text'),
        ('IMAGE', 'Image'),
    ]
    
    widget = models.ForeignKey(DocumentationWidget, related_name='items', on_delete=models.CASCADE)
    item_type = models.CharField(max_length=10, choices=ITEM_TYPES)
    text_content = models.TextField(blank=True, null=True)
    image = models.ImageField(upload_to='documentation_images/', blank=True, null=True)
    order = models.PositiveIntegerField(default=0)
    
    class Meta:
        ordering = ['order']
        
    def __str__(self):
        if self.item_type == 'TEXT':
            content = self.text_content[:50] + '...' if len(self.text_content) > 50 else self.text_content
            return f"Text: {content}"
        else:
            return f"Image: {self.image.name if self.image else 'No image'}"
