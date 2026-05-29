#3 task (1 variant)
import datetime

userYear = int(input("Enter your birth year"))
currentYear = datetime.date.today().year
userAge = currentYear - userYear
print(f"User age is {userAge}")