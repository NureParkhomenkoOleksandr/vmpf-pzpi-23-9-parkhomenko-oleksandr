#2 task (1 variant)
nums = 3
numsArr = []
for i in range(nums):
    temp = int(input("Enter number"))
    numsArr.append(temp)
numsSum = 0;
for num in numsArr:
    numsSum += num
print(f"Average of 3 numbers is {numsSum/nums}")