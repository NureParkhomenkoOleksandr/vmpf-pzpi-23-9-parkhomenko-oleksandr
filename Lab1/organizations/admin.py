from django.contrib import admin
from .models import Organization, Resource, Project, DonationCampaign


@admin.register(Organization)
class OrganizationAdmin(admin.ModelAdmin):
    list_display = ("name", "email", "phone", "volunteer_count")
    search_fields = ("name", "email", "phone")


@admin.register(Resource)
class ResourceAdmin(admin.ModelAdmin):
    list_display = ("name", "quantity", "unit", "organization")
    list_filter = ("organization",)
    search_fields = ("name",)


@admin.register(Project)
class ProjectAdmin(admin.ModelAdmin):
    list_display = ("title", "organization")
    list_filter = ("organization",)
    search_fields = ("title",)

@admin.register(DonationCampaign)
class DonationCampaignAdmin(admin.ModelAdmin):
    list_display = ("title", "organization", "goal_amount", "collected_amount")
    list_filter = ("organization",)
    search_fields = ("title",)