from django.contrib import admin
from .models import Category, Order, Product, Review, StoreUser


@admin.register(StoreUser)
class StoreUserAdmin(admin.ModelAdmin):
    list_display = ("username", "email", "phone", "address")
    search_fields = ("username", "email", "phone")


@admin.register(Category)
class CategoryAdmin(admin.ModelAdmin):
    list_display = ("name", "description")
    search_fields = ("name",)


@admin.register(Product)
class ProductAdmin(admin.ModelAdmin):
    list_display = ("name", "price", "stock_quantity", "category")
    search_fields = ("name", "description")
    list_filter = ("category",)


@admin.register(Order)
class OrderAdmin(admin.ModelAdmin):
    list_display = ("id", "user", "order_date", "status")
    search_fields = ("user__username", "user__email", "status")
    list_filter = ("status", "order_date")


@admin.register(Review)
class ReviewAdmin(admin.ModelAdmin):
    list_display = ("user", "product", "rating", "created_at")
    search_fields = ("user__username", "product__name", "text")
    list_filter = ("rating", "created_at")
