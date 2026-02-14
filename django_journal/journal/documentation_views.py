from rest_framework import viewsets, permissions, status
from rest_framework.response import Response
from django.contrib.contenttypes.models import ContentType
from .documentation import DocumentationWidget, DocumentationItem
from .documentation_serializers import DocumentationWidgetSerializer, DocumentationItemSerializer
from rest_framework.parsers import MultiPartParser, FormParser

class DocumentationWidgetViewSet(viewsets.ModelViewSet):
    queryset = DocumentationWidget.objects.all()
    serializer_class = DocumentationWidgetSerializer
    permission_classes = [permissions.IsAuthenticated]
    
    def get_queryset(self):
        queryset = super().get_queryset()
        parent_type = self.request.query_params.get('parent_type')
        parent_id = self.request.query_params.get('parent_id')
        
        if parent_type and parent_id:
            model_map = {
                'trade': 'trade',
                'setupstrategy': 'setupstrategy',
                'entrytype': 'entrytype'
            }
            
            model_name = model_map.get(parent_type.lower())
            if model_name:
                content_type = ContentType.objects.get(model=model_name)
                queryset = queryset.filter(content_type=content_type, object_id=parent_id)
        
        return queryset

class DocumentationItemViewSet(viewsets.ModelViewSet):
    queryset = DocumentationItem.objects.all()
    serializer_class = DocumentationItemSerializer
    permission_classes = [permissions.IsAuthenticated]
    parser_classes = [MultiPartParser, FormParser]
    
    def create(self, request, *args, **kwargs):
        parent_type = request.data.get('parent_type')
        parent_id = request.data.get('parent_id')
        
        # If parent_type and parent_id are provided, find or create the appropriate widget
        if parent_type and parent_id:
            model_map = {
                'trade': 'trade',
                'setupstrategy': 'setupstrategy',
                'entrytype': 'entrytype'
            }
            
            model_name = model_map.get(parent_type.lower())
            if not model_name:
                return Response(
                    {"error": f"Invalid parent_type: {parent_type}"}, 
                    status=status.HTTP_400_BAD_REQUEST
                )
                
            content_type = ContentType.objects.get(model=model_name)
            
            # Get or create widget
            widget, created = DocumentationWidget.objects.get_or_create(
                content_type=content_type,
                object_id=parent_id
            )
            
            # Add widget to request data
            mutable_data = request.data.copy()
            mutable_data['widget'] = widget.id
            
            # Create serializer with modified data
            serializer = self.get_serializer(data=mutable_data)
            serializer.is_valid(raise_exception=True)
            self.perform_create(serializer)
            
            headers = self.get_success_headers(serializer.data)
            return Response(serializer.data, status=status.HTTP_201_CREATED, headers=headers)
        
        # If no parent info, proceed normally
        return super().create(request, *args, **kwargs)
