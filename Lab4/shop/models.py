from django.db import models


class StoreUser(models.Model):
    username = models.CharField(max_length=150)
    email = models.EmailField(unique=True)
    phone = models.CharField(max_length=20)
    address = models.TextField()

    def __str__(self):
        return self.username

    @classmethod
    def create_user(cls, username, email, phone, address):
        return cls.objects.create(
            username=username,
            email=email,
            phone=phone,
            address=address,
        )

    def update_user(self, username=None, email=None, phone=None, address=None):
        if username is not None:
            self.username = username
        if email is not None:
            self.email = email
        if phone is not None:
            self.phone = phone
        if address is not None:
            self.address = address
        self.save()
        return self

    def delete_user(self):
        self.delete()


class Category(models.Model):
    name = models.CharField(max_length=100)
    description = models.TextField(blank=True)

    def __str__(self):
        return self.name

    @classmethod
    def create_category(cls, name, description=""):
        return cls.objects.create(name=name, description=description)

    def update_category(self, name=None, description=None):
        if name is not None:
            self.name = name
        if description is not None:
            self.description = description
        self.save()
        return self

    def delete_category(self):
        self.delete()


class Product(models.Model):
    name = models.CharField(max_length=150)
    description = models.TextField(blank=True)
    price = models.DecimalField(max_digits=10, decimal_places=2)
    stock_quantity = models.PositiveIntegerField()
    category = models.ForeignKey(
        Category,
        on_delete=models.CASCADE,
        related_name="products",
    )

    def __str__(self):
        return self.name

    @classmethod
    def create_product(cls, name, description, price, stock_quantity, category):
        return cls.objects.create(
            name=name,
            description=description,
            price=price,
            stock_quantity=stock_quantity,
            category=category,
        )

    def update_product(
        self,
        name=None,
        description=None,
        price=None,
        stock_quantity=None,
        category=None,
    ):
        if name is not None:
            self.name = name
        if description is not None:
            self.description = description
        if price is not None:
            self.price = price
        if stock_quantity is not None:
            self.stock_quantity = stock_quantity
        if category is not None:
            self.category = category
        self.save()
        return self

    def delete_product(self):
        self.delete()


class Order(models.Model):
    user = models.ForeignKey(
        StoreUser,
        on_delete=models.CASCADE,
        related_name="orders",
    )
    products = models.ManyToManyField(Product, related_name="orders")
    order_date = models.DateTimeField(auto_now_add=True)
    status = models.CharField(max_length=50, default="new")

    def __str__(self):
        return f"Order #{self.id} - {self.user.username}"

    @classmethod
    def create_order(cls, user, products=None, status="new"):
        order = cls.objects.create(user=user, status=status)
        if products is not None:
            order.products.set(products)
        return order

    def update_order(self, user=None, products=None, status=None):
        if user is not None:
            self.user = user
        if status is not None:
            self.status = status
        self.save()
        if products is not None:
            self.products.set(products)
        return self

    def delete_order(self):
        self.delete()


class Review(models.Model):
    user = models.ForeignKey(
        StoreUser,
        on_delete=models.CASCADE,
        related_name="reviews",
    )
    product = models.ForeignKey(
        Product,
        on_delete=models.CASCADE,
        related_name="reviews",
    )
    text = models.TextField()
    rating = models.PositiveSmallIntegerField()
    created_at = models.DateTimeField(auto_now_add=True)

    def __str__(self):
        return f"{self.product.name} - {self.rating}/5"

    @classmethod
    def create_review(cls, user, product, text, rating):
        return cls.objects.create(
            user=user,
            product=product,
            text=text,
            rating=rating,
        )

    def update_review(self, user=None, product=None, text=None, rating=None):
        if user is not None:
            self.user = user
        if product is not None:
            self.product = product
        if text is not None:
            self.text = text
        if rating is not None:
            self.rating = rating
        self.save()
        return self

    def delete_review(self):
        self.delete()
