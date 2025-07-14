package bigInt;

import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class VeryBig {
	private static Deque<Character> inputStack = new ArrayDeque<>();
	private static Deque<String> resultStack = new ArrayDeque<>();
	private static Deque<Character> signStack = new ArrayDeque<>();
	private static Deque<Character> minusStack = new ArrayDeque<>();
	private static Deque<Character> digitStack = new ArrayDeque<>();
	private static Deque<Character> variableStack = new ArrayDeque<>();
	
	private static HashMap<String, String> varValues = new HashMap<>();
	private static boolean isUnaryBeforeParentheses = false;
	
	private static void doTheJob(String input) {
		input = input.trim().replaceAll("\\s+", "");		
		if (input.matches("-?\\d+")) { 
			System.out.println(input);
		}
		else if (input.matches("[\\w]+[=]{1}[()\\w*\\/^+-]+")) {
			assign(input);
		}		
		else if (input.matches(".+[*/^+-].+")) {
			String result = getResult(input).pop(); 
			if(result != "") {
				System.out.println(result);
			}
			resultStack.clear();
		}
		else if (input.matches("[a-zA-Z]+")) {
				if (getValue(input) != "") {
					System.out.println(getValue(input));
				}
				else {
					System.out.println("Unknown variable"); 
				}
		}
		else if (input.matches("\\w*(\\d[A-Za-z]|[A-Za-z]\\d)\\w*")){
			System.out.println("Invalid identifier");	 
		}
	}
	
	private static Deque<String> getResult(String input) {
		Character original = '\u0000';	
		char[] charArray = input.toCharArray();
		int countOpeningBrackets = 0;
		int countClosingBrackets = 0;
		for (int i = 0; i < charArray.length; i++) {
			inputStack.push(charArray[i]);
			if (charArray[i] == '(')
				countOpeningBrackets++;
			if (charArray[i] == ')')
				countClosingBrackets++;
				if ((i == charArray.length-1 && countOpeningBrackets != countClosingBrackets) || (!(Character.toString(charArray[charArray.length-1])).matches("[a-zA-Z0-9]|\\)"))) {
					inputStack.pop();
					System.out.println("Invalid expression"); 
                    inputStack.clear();
					resultStack.push("");
					break;
				}
			}
			if (inputStack.size() == charArray.length) {				
				while (inputStack.size() > 0) {										
					original = inputStack.pollLast();
					
					// VARIABLE
					if (Character.toString(original).matches("[a-zA-Z]")) {
						String variable = "";
						variableStack.push(original);						
						if (inputStack.size() > 0) {
							Character next = inputStack.peekLast();		
							if (!(Character.toString(next)).matches("[a-zA-Z)*/^+-]")) {
								System.out.println("Invalid expression"); 
                                inputStack.clear();
								resultStack.push("");
								break; 
							}
							else if ((Character.toString(next)).matches("[)*/^+-]")) {
								while (variableStack.size() > 0) {
									variable += variableStack.pollLast();								
								}								
							}						
						}
						else {
							while (variableStack.size() > 0) {
								variable += variableStack.pollLast();			
							}						
						}
						if (variable.startsWith("-")) {
							variable = variable.substring(1);
							if (isAssigned(variable)) {
								BigInteger varInt = new BigInteger(getValue(variable));  
								if (varInt.compareTo(BigInteger.ZERO) < 0) {
									resultStack.push(getValue(variable).substring(1));
								}
								else {
									resultStack.push("-"+getValue(variable));
								}																			
							}
							else {
								System.out.println("Unknown variable"); 
								resultStack.push("");
                                inputStack.clear();
								break;
							}
						}
						else {
							if (isAssigned(variable)) {						
								resultStack.push(getValue(variable));								
							}
							else {
								System.out.println("Unknown variable"); 
								resultStack.push("");
                                inputStack.clear();
								break;
							}
						}
						variableStack.clear();
					}
			
					// NUMBER
					if (Character.toString(original).matches("\\d")) {						
						String num = "";
						digitStack.push(original);
						if (inputStack.size() > 0) {
							Character next = inputStack.peekLast();							
							if (!(Character.toString(next)).matches("\\d|[)*/^+-]")) {
								System.out.println("Invalid expression"); 
								resultStack.push("");
                                inputStack.clear();
								break; 
							}
							else if ((Character.toString(next)).matches("[)*/^+-]")) {
								while (digitStack.size() > 0) {
									num += digitStack.pollLast();								
								}
							}
						}
						else {
							while (digitStack.size() > 0) {
								num += digitStack.pollLast();			
							}						
						}
						if (!num.equals("")) {
							resultStack.push(num);
						}
					}
								
					// SIGN (
					else if (original =='(') {						
						Character next = inputStack.peekLast();						
						if (Character.toString(next).matches("[a-zA-Z0-9(-]")) { 							
							signStack.push(original);							
							if (next == '-') {								
								next = inputStack.pollLast();								
								Character nextNew = inputStack.peekLast();
								if (Character.toString(nextNew).matches("[0-9]")) { 
									digitStack.push(next);									
								}
								else if (Character.toString(nextNew).matches("[a-zA-Z]")) { 
									variableStack.push(next);									
								}
							}
						}
						else {
							System.out.println("Invalid expression");
							resultStack.push("");
                            inputStack.clear();
							break;
						}
					}	
					
					// SIGN )
					else if (original ==')') {
						while (signStack.peek() != '(' && signStack.size() > 0) {							
								callCalculate();									
						}
						if(signStack.peek() == '(') {
							if (isUnaryBeforeParentheses) {
								String last = resultStack.pop();
								if (last.startsWith("-")) {
									last = last.substring(1);
								}
								else {
									last = "-" + last;
								}
								resultStack.push(last);
								isUnaryBeforeParentheses = false;
							}
							signStack.pop();
						}
					}
					
					// ALL THE OTHERS	
					else if (Character.toString(original).matches("[*/+-]")) {
						Character toBeOperator = getOperator(original, charArray);
						if (toBeOperator == '!') {
							System.out.println("Invalid expression");
							resultStack.push("");
							inputStack.clear();
							break;
						}
						else {
							if (toBeOperator != '\u0000') {
								if (signStack.size() == 0)
									signStack.push(toBeOperator);
								else if (signStack.size() > 0 && getOperatorPrecedence().get(toBeOperator) > getOperatorPrecedence().get(signStack.peek())) {
									signStack.push(toBeOperator);
								}
								else {
									while (signStack.size() > 0 && (getOperatorPrecedence().get(toBeOperator) < getOperatorPrecedence().get(signStack.peek()) || getOperatorPrecedence().get(toBeOperator) == getOperatorPrecedence().get(signStack.peek()))) {										
											callCalculate();												
										}
										signStack.push(toBeOperator);
									}							
								}
							}
						}
					}			
					while (signStack.size() > 0) {						
						callCalculate();		
					}				
			}
			return resultStack;			
	}
				
	private static Map<Character, Integer>	getOperatorPrecedence() {
		Map<Character, Integer> signPrecedence = new HashMap<>();	
		signPrecedence.put('(', 0);
		signPrecedence.put('-', 1);
		signPrecedence.put('+', 1);
		signPrecedence.put('/', 2);
		signPrecedence.put('*', 2);
		signPrecedence.put('^', 3);
		return signPrecedence;
	}
	private static Character getOperator(Character original, char[] charArray) {
		Character operator = '\u0000';
		//PLUS
		if (original =='+') { 				
			while (inputStack.peekLast() == '+') {
				inputStack.pollLast();
			}
			if (!(Character.toString(inputStack.peekLast())).matches("[(a-zA-Z0-9]")) {
				operator = '!';
			}
			else if ((Character.toString(inputStack.peekLast())).matches("[(a-zA-Z0-9]")) {
				operator = '+';
			}	
		}
		//MINUS
		else if (original =='-') {
			if (inputStack.size() == charArray.length-1) { // charArray[0] = '-'   
				if ((Character.toString(inputStack.peekLast())).matches("[0-9]")) {						
					digitStack.push(original);		
					operator = '\u0000';
				}
				else if ((Character.toString(inputStack.peekLast())).matches("[a-zA-Z]")) {						
					variableStack.push(original);		
					operator = '\u0000';
				}
				else {
					operator = '!';
				}
			}
			else { 
				minusStack.push(original); 
				while (inputStack.peekLast() == '-') {
					minusStack.push(inputStack.pollLast());
				}
				if ((Character.toString(inputStack.peekLast())).matches("[a-zA-Z0-9(]")) {					
					operator = getOperatorOfMinusSigns(minusStack.size());
					minusStack.clear();
				}			
				  else { 
					  operator = '!'; 
				  }						 
			 }					
		}
		//THE OTHERS
		else {			
			Character next = inputStack.peekLast(); 
			if ((Character.toString(next)).matches("[a-zA-Z0-9(-]")) {
				operator = original;
			}
			if (next == '-') {					
				Character unary = inputStack.pollLast();
				if ((Character.toString(inputStack.peekLast())).matches("[a-zA-Z0-9]")) {					
					digitStack.push(unary); 
					operator = original;	
				}
				else if ((Character.toString(inputStack.peekLast())).matches("[(]")) {
					isUnaryBeforeParentheses = true;					
				}
				else {
					operator = '!';
				} 
			}
			else if (!(Character.toString(next)).matches("[a-zA-Z0-9(-]")) {
				operator = '!';
			} 					
		} 
		return operator;
	}
									
	private static char getOperatorOfMinusSigns(int count) {
		 char ch = '\u0000';
		 if (count % 2 == 0) 
			 ch = '+';		 
		 else 
			 ch = '-'; 
		 return ch;
	}

	private static BigInteger calculate(BigInteger a, BigInteger b, char ch) {
		BigInteger result = BigInteger.ZERO;
		switch (ch) {
			 case '+':
		            result = a.add(b); 
		            break;
			 case '-':
		            result = a.subtract(b);
		            break;
			 case '*':
		            result = a.multiply(b);
		            break;
			 case '/':
		            result = a.divide(b);
		            break;
			}
			return result;
	}
	private static void callCalculate() {
			Character operator = signStack.pop();
			String second ="";
			String first ="";
			if (resultStack.peek() != "") {
				second = resultStack.pop();
			}
			if (resultStack.peek() != "") {
				first = resultStack.pop(); 
			}
			if (first != "" && second != "") {
				BigInteger operand1 = new BigInteger(first);
				BigInteger operand2 = new BigInteger(second);
				
				if (operand2.compareTo(BigInteger.ZERO) == 0 && operator == '/') {
					System.out.println("Zero cannot be a divisor");
					resultStack.push("");					
				}
				else {
					calculate (operand1, operand2, operator);
					resultStack.push(String.valueOf(calculate (operand1, operand2, operator)));		
				}				 
			}					
	}
	private static void assign(String input) { 
		List<String> parts = new ArrayList<String>(Arrays.asList(input.split("=")));
		if (parts.contains("")) {
			parts.remove(""); 
		}
		if (parts.size() == 2) { 
			if (!(parts.get(0).matches("[a-zA-Z]+"))) {
				System.out.println("Invalid identifier"); 
			}
			else { 
				if (parts.get(1).matches("(-?|\\+?)\\d+")) {
					varValues.put(parts.get(0), parts.get(1)); 
				}
				else if (parts.get(1).matches("[a-zA-Z]+")) {
					if (isAssigned(parts.get(1))) {
						String value = getValue(parts.get(1));
						if (value != null) {
						varValues.put(parts.get(0), getValue(parts.get(1)));
						}						
					}
					else {
						System.out.println("Unknown variable");
					}
				}
				else if (parts.get(1).matches(".+[()*/^+-]+.+")) {
					String result = getResult(parts.get(1)).pop();
					if(result != "") {
						varValues.put(parts.get(0), result);
					}					
				}
				else {
					System.out.println("Invalid assignment");
				}	
			}
		}		
	}

	private static String getValue(String variable) {
		String value = "";
		if (varValues.get(variable) != null) {
			value = varValues.get(variable);
		}
		return value; 
	}
	private static boolean isAssigned(String variable) {
		if(varValues.containsKey(variable)) {
			if (varValues.get(variable) != null) {
				return true;
			}
		}
		return false;
	}
	
	public static void main(String[] args) {	
		Scanner scanner = new Scanner(System.in);
	    while (scanner.hasNextLine()) {
	        String line = scanner.nextLine();
	        if (!(line.trim().isEmpty())) {	                
	            if (line.matches("\\s?/.*")) {
	                if (line.equals("/exit")) {
	                    System.out.println("Bye!");
	                    break;
		                }
	                 else if  (line.equals("/help")) {
	                    System.out.println("The program calculates the sum, difference, product and quotient of integer numbers and assigned variables \nof values in the range -2 ^ Integer.MAX_VALUE (exclusive) to +2 ^ Integer.MAX_VALUE (exclusive)");
	                 }	               
	                 else {
	                	 System.out.println("Unknown command");
		                 }
		            }	            
		            else {  
		            	doTheJob(line);
				    }       
	        }
	    }
	    scanner.close();
	}
}



