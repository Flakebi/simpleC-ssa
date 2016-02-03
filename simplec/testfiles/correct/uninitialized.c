
int foo(){
  int i = 42;
  return i;
}
int main(){
  int i;
  i = 5;
  if (i==5) 
    foo(); // triggers a MethdoCall Transition
  else    
    i=foo(); // triggers an Assignment with an embedded MethodCall Expression!
  while(i==4711) {
    i=42-42;
  }

  return i;
}
