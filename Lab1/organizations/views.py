from django.shortcuts import render, redirect, get_object_or_404
from .models import Organization, DonationCampaign, Project, Resource
from .forms import DonationCampaignForm
from django.db.models import Sum


def organization_list(request):
    organizations = Organization.objects.all()
    return render(request, "organizations/organization_list.html", {"organizations": organizations})


def campaign_list(request):
    campaigns = DonationCampaign.objects.all()

    search = request.GET.get("search")
    organization = request.GET.get("organization")
    sort = request.GET.get("sort")

    if search:
        campaigns = campaigns.filter(title__icontains=search)

    if organization:
        campaigns = campaigns.filter(organization_id=organization)

    if sort == "goal":
        campaigns = campaigns.order_by("-goal_amount")
    elif sort == "collected":
        campaigns = campaigns.order_by("-collected_amount")
    elif sort == "title":
        campaigns = campaigns.order_by("title")

    organizations = Organization.objects.all()

    return render(request, "organizations/campaign_list.html", {
        "campaigns": campaigns,
        "organizations": organizations,
    })


def campaign_create(request):
    form = DonationCampaignForm(request.POST or None)

    if form.is_valid():
        form.save()
        return redirect("campaign_list")

    return render(request, "organizations/campaign_form.html", {"form": form})


def campaign_update(request, campaign_id):
    campaign = get_object_or_404(DonationCampaign, id=campaign_id)
    form = DonationCampaignForm(request.POST or None, instance=campaign)

    if form.is_valid():
        form.save()
        return redirect("campaign_list")

    return render(request, "organizations/campaign_form.html", {"form": form})


def campaign_delete(request, campaign_id):
    campaign = get_object_or_404(DonationCampaign, id=campaign_id)

    if request.method == "POST":
        campaign.delete()
        return redirect("campaign_list")

    return render(request, "organizations/campaign_confirm_delete.html", {"campaign": campaign})

def reports(request):
    organizations = Organization.objects.all()

    organization_count = Organization.objects.count()
    project_count = Project.objects.count()
    resource_count = Resource.objects.count()
    campaign_count = DonationCampaign.objects.count()

    total_goal = DonationCampaign.objects.aggregate(Sum("goal_amount"))["goal_amount__sum"] or 0
    total_collected = DonationCampaign.objects.aggregate(Sum("collected_amount"))["collected_amount__sum"] or 0

    if total_goal > 0:
        percent = round((total_collected / total_goal) * 100, 2)
    else:
        percent = 0

    return render(request, "organizations/reports.html", {
        "organizations": organizations,
        "organization_count": organization_count,
        "project_count": project_count,
        "resource_count": resource_count,
        "campaign_count": campaign_count,
        "total_goal": total_goal,
        "total_collected": total_collected,
        "percent": percent,
    })