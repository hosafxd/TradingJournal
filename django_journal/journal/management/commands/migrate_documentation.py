from django.core.management.base import BaseCommand
from django.contrib.contenttypes.models import ContentType
from journal.models import DocumentationBlock, Trade, SetupStrategy, EntryType
from journal.documentation import DocumentationWidget, DocumentationItem

class Command(BaseCommand):
    help = 'Migrates existing DocumentationBlock data to the new DocumentationWidget system'

    def handle(self, *args, **options):
        self.stdout.write('Starting documentation migration...')
        
        # Migrate Trade documentation
        self.migrate_model_documentation(Trade, 'trade')
        
        # Migrate SetupStrategy documentation
        self.migrate_model_documentation(SetupStrategy, 'setupstrategy')
        
        # Migrate EntryType documentation
        self.migrate_model_documentation(EntryType, 'entrytype')
        
        self.stdout.write(self.style.SUCCESS('Documentation migration completed successfully!'))
    
    def migrate_model_documentation(self, model_class, model_name):
        """Migrate documentation blocks for a specific model to the new system"""
        self.stdout.write(f'Migrating {model_name} documentation...')
        content_type = ContentType.objects.get(model=model_name)
        count = 0
        
        # Get all instances of the model
        instances = model_class.objects.all()
        
        for instance in instances:
            # Get all documentation blocks for this instance
            doc_blocks = DocumentationBlock.objects.filter(
                content_type=ContentType.objects.get_for_model(instance),
                object_id=instance.id
            ).order_by('order')
            
            if not doc_blocks:
                continue
                
            # Create a new documentation widget for this instance
            widget = DocumentationWidget.objects.create(
                content_type=content_type,
                object_id=instance.id,
                order=0
            )
            
            # Migrate each documentation block to the new system
            for i, block in enumerate(doc_blocks):
                item_type = 'IMAGE' if block.block_type == 'image' else 'TEXT'
                
                # Create a new documentation item
                DocumentationItem.objects.create(
                    widget=widget,
                    item_type=item_type,
                    text_content=block.text_content if item_type == 'TEXT' else None,
                    image=block.image_content if item_type == 'IMAGE' else None,
                    order=block.order or i
                )
                count += 1
                
        self.stdout.write(f'Migrated {count} documentation blocks for {model_name}')
