from django import forms
from .models import DonationCampaign


class DonationCampaignForm(forms.ModelForm):
    class Meta:
        model = DonationCampaign
        fields = ["title", "description", "goal_amount", "collected_amount", "organization"]