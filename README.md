Town (Roblox Game) wiring language
An example of simple code here would be:

button(a){
 transmit(a)
}
group{
 button(color1)
 force(T_if_F)
}
group{
 receive(color1)
}


And a more complex example:
(there are only single line comments: //)

group{
    transmit(P1)
} //Group structure starts and ends must be on their own lines. (You cannot do group{func})
group{
    transmit(P2)
}
nextLine //nextLine used to go to the next line (Usually by using groups it increments the X,
         //this increments the Z and sets the X to 0)
group{ //A group has a wire type 1 and 2 and a simple block, forming a simple move mechanism.
    receive(P1)
    receive(P2)
    logic(AND) //Logic has multiple modes: OR|AND|NOT|XOR|NOR|NAND|XNOR
    transmit(end)
    force(T_if_F) //Force also has multiple modes: T_if_F|F_if_F|T_if_T|F_if_T|Always|Never
    //a force gate takes the signal a group transmits and changes it based on the mode it has: 
    //T_if_F will make it output True if the input is False (does not do anything if the input is True)
    //Always returns POSITIVE when getting any type of input.
}
nextLine
group{
    receive(end)
}
//Now a representation of functions (keep in mind that functions can only be called after declared)
button(a b){
    transmit(a)
    transmit(b)
}
//Calling it like this embeds it into the group
group{
    button(hello hello)
    receive(hello)
}
nextLine
//Calling it like this simply runs the function
button(hello hello)

