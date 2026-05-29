from django.urls import path
from . import views

urlpatterns = [
    path("", views.organization_list, name="organization_list"),

    path("campaigns/", views.campaign_list, name="campaign_list"),
    path("campaigns/add/", views.campaign_create, name="campaign_create"),
    path("campaigns/<int:campaign_id>/edit/", views.campaign_update, name="campaign_update"),
    path("campaigns/<int:campaign_id>/delete/", views.campaign_delete, name="campaign_delete"),

    path("reports/", views.reports, name="reports"),
]