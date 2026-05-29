#4 task (1 variant)
import datetime
class Book:
    def __init__(self, title, author, year):
        self.title = title
        self.author = author
        self.year = year

    def display_info(self):
        print(f"Title: {self.title}, Author: {self.author}, Year: {self.year}")

book = Book("Siesta", "Utah Ranger", datetime.datetime.now().year)
book.display_info()