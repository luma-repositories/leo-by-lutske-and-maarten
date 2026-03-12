question (0003):
The correct approach is to use the record's canonical constructor and perform the validation within it. However, the user's code is trying to add a constructor with the same name but without parameters, which is not allowed.
So the solution is to remove the explicit constructor and perform the validation in the record's canonical constructor. But how? Because records don't allow instance initial

Answer:
