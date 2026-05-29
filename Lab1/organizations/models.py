from django.db import models

class Organization(models.Model):
    name = models.CharField(max_length=100)
    email = models.EmailField()
    phone = models.CharField(max_length=20)
    address = models.TextField()
    website = models.URLField(blank=True)
    description = models.TextField()
    volunteer_count = models.PositiveIntegerField()

    def __str__(self):
        return self.name

class Resource(models.Model):
    name = models.CharField(max_length=100)
    quantity = models.IntegerField()
    unit = models.CharField(max_length=100)
    organization = models.ForeignKey(Organization, on_delete=models.CASCADE)

    def __str__(self):
        return self.name

class Project(models.Model):
    title = models.CharField(max_length=100)
    description = models.TextField()
    organization = models.ForeignKey(Organization, on_delete=models.CASCADE)

    def __str__(self):
        return self.title

class DonationCampaign(models.Model):
    title = models.CharField(max_length=100)
    description = models.TextField()
    goal_amount = models.DecimalField(max_digits=10, decimal_places=2)
    collected_amount = models.DecimalField(max_digits=10, decimal_places=2)
    organization = models.ForeignKey(Organization, on_delete=models.CASCADE)

    def __str__(self):
        return self.title