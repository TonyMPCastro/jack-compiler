package br.ufma.ecp;

import static org.junit.Assert.assertEquals;

import java.nio.charset.StandardCharsets;

import org.junit.Test;

public class VMWriterTest extends TestSupport {

    @Test
    public void testInt() {
        var input = """
                10
                """;

        var parser = new Parser(input.getBytes(StandardCharsets.UTF_8));
        parser.parseExpression();
        String actual = parser.VMOutput();
        String expected = """
                push constant 10
                    """;
        assertEquals(expected, actual);
    }

    @Test
    public void testSimpleExpression() {
        var input = """
                10 + 30
                """;

        var parser = new Parser(input.getBytes(StandardCharsets.UTF_8));
        parser.parseExpression();
        String actual = parser.VMOutput();
        String expected = """
                push constant 10
                push constant 30
                add
                    """;
        assertEquals(expected, actual);
    }

    @Test
    public void testLiteralString() {
        var input = """
                "OLA"
                """;

        var parser = new Parser(input.getBytes(StandardCharsets.UTF_8));
        parser.parseExpression();
        String actual = parser.VMOutput();
        String expected = """
                push constant 3
                call String.new 1
                push constant 79
                call String.appendChar 2
                push constant 76
                call String.appendChar 2
                push constant 65
                call String.appendChar 2
                    """;
        assertEquals(expected, actual);
    }

    @Test
    public void testFalse() {
        var input = """
                false
                """;

        var parser = new Parser(input.getBytes(StandardCharsets.UTF_8));
        parser.parseExpression();
        String actual = parser.VMOutput();
        String expected = """
                push constant 0
                    """;
        assertEquals(expected, actual);
    }

    @Test
    public void testNull() {
        var input = """
                null
                """;

        var parser = new Parser(input.getBytes(StandardCharsets.UTF_8));
        parser.parseExpression();
        String actual = parser.VMOutput();
        String expected = """
                push constant 0
                    """;
        assertEquals(expected, actual);
    }

    @Test
    public void testTrue() {
        var input = """
                true
                """;

        var parser = new Parser(input.getBytes(StandardCharsets.UTF_8));
        parser.parseExpression();
        String actual = parser.VMOutput();
        String expected = """
                push constant 0
                not
                    """;
        assertEquals(expected, actual);
    }

    @Test
    public void testThis() {
        var input = """
                this
                """;

        var parser = new Parser(input.getBytes(StandardCharsets.UTF_8));
        parser.parseExpression();
        String actual = parser.VMOutput();
        String expected = """
                push pointer 0
                    """;
        assertEquals(expected, actual);
    }

    @Test
    public void testNot() {
        var input = """
                ~ false
                """;

        var parser = new Parser(input.getBytes(StandardCharsets.UTF_8));
        parser.parseExpression();
        String actual = parser.VMOutput();
        String expected = """
                push constant 0
                not
                    """;
        assertEquals(expected, actual);
    }

    @Test
    public void testMinus() {
        var input = """
                - 10
                """;

        var parser = new Parser(input.getBytes(StandardCharsets.UTF_8));
        parser.parseExpression();
        String actual = parser.VMOutput();
        String expected = """
                push constant 10
                neg
                    """;
        assertEquals(expected, actual);
    }

    @Test
    public void testReturn() {
        var input = """
                return;
                """;

        var parser = new Parser(input.getBytes(StandardCharsets.UTF_8));
        parser.parseStatement();
        String actual = parser.VMOutput();
        String expected = """
                push constant 0
                return
                    """;
        assertEquals(expected, actual);
    }

    @Test
    public void testReturnExpr() {
        var input = """
                return 10;
                """;

        var parser = new Parser(input.getBytes(StandardCharsets.UTF_8));
        parser.parseStatement();
        String actual = parser.VMOutput();
        String expected = """
                push constant 10
                return
                    """;
        assertEquals(expected, actual);
    }

    @Test
    public void testIf() {
        var input = """
                if (false) {
                    return 10;
                } else {
                    return 20;
                }
                """;

        var parser = new Parser(input.getBytes(StandardCharsets.UTF_8));
        parser.parseStatement();
        String actual = parser.VMOutput();
        String expected = """
                push constant 0
                if-goto IF_TRUE0
                goto IF_FALSE0
                label IF_TRUE0
                push constant 10
                return
                goto IF_END0
                label IF_FALSE0
                push constant 20
                return
                label IF_END0
                        """;
        assertEquals(expected, actual);
    }

    @Test
    public void testWhile() {
        var input = """
                while (false) {
                    return 10;
                }
                """;

        var parser = new Parser(input.getBytes(StandardCharsets.UTF_8));
        parser.parseStatement();
        String actual = parser.VMOutput();
        String expected = """
                label WHILE_EXP0
                push constant 0
                not
                if-goto WHILE_END0
                push constant 10
                return
                goto WHILE_EXP0
                label WHILE_END0
                                    """;
        assertEquals(expected, actual);
    }

    @Test
    public void testSimpleFunctions() {
        var input = """
                class Main {

                    function int soma (int x, int y) {
                            return  30;
                     }

                     function void main () {
                            var int d;
                            return;
                      }

                    }
                """;
        ;
        var parser = new Parser(input.getBytes(StandardCharsets.UTF_8));
        parser.parse();
        String actual = parser.VMOutput();
        String expected = """
                function Main.soma 0
                push constant 30
                return
                function Main.main 1
                push constant 0
                return
                    """;
        assertEquals(expected, actual);
    }

    @Test
    public void testSimpleFunctionWithVar() {
        var input = """
                class Main {

                     function int funcao () {
                            var int d;
                            return d;
                      }

                    }
                """;
        ;
        var parser = new Parser(input.getBytes(StandardCharsets.UTF_8));
        parser.parse();
        String actual = parser.VMOutput();
        String expected = """
                function Main.funcao 1
                push local 0
                return
                """;
        assertEquals(expected, actual);
    }

    @Test
    public void testLet() {
        var input = """
                class Main {

                  function void main () {
                      var int x;
                      let x = 42;
                      return;
                  }
                }
                """;
        var parser = new Parser(input.getBytes(StandardCharsets.UTF_8));
        parser.parse();
        String actual = parser.VMOutput();
        String expected = """
                function Main.main 1
                push constant 42
                pop local 0
                push constant 0
                return
                    """;
        assertEquals(expected, actual);
    }

    @Test
    public void testLet2() {
        var input = """
            class Main {
                function void main () {
                    var Point p;
                    let p = Point.new(10, 20);
                    return;
                }
            }
            """;;
        
        var parser = new Parser(input.getBytes(StandardCharsets.UTF_8));
        parser.parse();
        String actual = parser.VMOutput();
        String expected = """
            function Main.main 1
            push constant 10
            push constant 20
            call Point.new 2
            pop local 0
            push constant 0
            return
            """;
        assertEquals(expected, actual);
    }

    @Test
    public void arrayTest() {
        var input = """
                class Main {
                    function void main () {
                        var Array v;
                        let v[2] = v[3] + 42;
                        return;
                    }
                }
                """;
        ;
        var parser = new Parser(input.getBytes(StandardCharsets.UTF_8));
        parser.parse();
        String actual = parser.VMOutput();
        String expected = """
                function Main.main 1
                push constant 2
                push local 0
                add
                push constant 3
                push local 0
                add
                pop pointer 1
                push that 0
                push constant 42
                add
                pop temp 0
                pop pointer 1
                push temp 0
                pop that 0
                push constant 0
                return
                    """;
        assertEquals(expected, actual);
    }

    @Test
    public void constructorTest() {
        var input = """
                class Point {
                	field int x, y;
                	static int pointCount;
                	constructor Point new(int ax, int ay) {
                		let x = ax;
                		let y = ay;
                		let pointCount = pointCount + 1;
                		return this;
                	}
                }
        """;
        var parser = new Parser(input.getBytes(StandardCharsets.UTF_8));
        parser.parse();
        String actual = parser.VMOutput();
        String expected = """
                function Point.new 0
                push constant 2
                call Memory.alloc 1
                pop pointer 0
                push argument 0
                pop this 0
                push argument 1
                pop this 1
                push static 0
                push constant 1
                add
                pop static 0
                push pointer 0
                return
                """;
        assertEquals(expected, actual);
    }

    @Test
    public void callFunctionTest() {

        var input = """
            class Main {
                function int soma (int x, int y) {
                       return  x + y;
                }
               
                function void main () {
                       var int d;
                       let d = Main.soma(4,5);
                       return;
                 }
               
               }
            """;;
        var parser = new Parser(input.getBytes(StandardCharsets.UTF_8));
        parser.parse();


        String actual = parser.VMOutput();
        String expected = """
            function Main.soma 0
            push argument 0
            push argument 1
            add
            return
            function Main.main 1
            push constant 4
            push constant 5
            call Main.soma 2
            pop local 0
            push constant 0
            return
                """;
        assertEquals(expected, actual); 
    }

    @Test
    public void methodTest () {
        var input = """
            class Main {
                function void main () {
                    var Point p;
                    var int x;
                    let p = Point.new(10, 20);
                    let x = p.getX();
                    return;
                }
            }
            """;;
        var parser = new Parser(input.getBytes(StandardCharsets.UTF_8));
        parser.parse();
        String actual = parser.VMOutput();
        String expected = """
            function Main.main 2
            push constant 10
            push constant 20
            call Point.new 2
            pop local 0
            push local 0
            call Point.getX 1
            pop local 1
            push constant 0
            return
            """;
        assertEquals(expected, actual);
    }

    @Test
    public void doStatement () {
        var input = """
            class Main {
                function void main () {
                    var int x;
                    let x = 10;
                    do Output.printInt(x);
                    return;
                }
            }
            """;;
        var parser = new Parser(input.getBytes(StandardCharsets.UTF_8));
        parser.parse();
        String actual = parser.VMOutput();
        String expected = """
            function Main.main 1
            push constant 10
            pop local 0
            push local 0
            call Output.printInt 1
            pop temp 0
            push constant 0
            return
                """;
        assertEquals(expected, actual);
    }


    @Test
    public void methodsConstructorTest () {
        var input = """
            class Point {
                field int x, y;
            
                method int getX () {
                    return x;
                }
            
                method int getY () {
                    return y;
                }
            
                method void print () {
                    do Output.printInt(getX());
                    do Output.printInt(getY());
                    return;
                }
            
                constructor Point new(int Ax, int Ay) { 
                  var int w;             
                  let x = Ax;
                  let y = Ay;
                  let w = 42;
                  let x = w;
                  return this;
               }
            }
            """;;
        var parser = new Parser(input.getBytes(StandardCharsets.UTF_8));
        parser.parse();
        String actual = parser.VMOutput();
        String expected = """
            function Point.getX 0
            push argument 0
            pop pointer 0
            push this 0
            return
            function Point.getY 0
            push argument 0
            pop pointer 0
            push this 1
            return
            function Point.print 0
            push argument 0
            pop pointer 0
            push pointer 0
            call Point.getX 1
            call Output.printInt 1
            pop temp 0
            push pointer 0
            call Point.getY 1
            call Output.printInt 1
            pop temp 0
            push constant 0
            return
            function Point.new 1
            push constant 2
            call Memory.alloc 1
            pop pointer 0
            push argument 0
            pop this 0
            push argument 1
            pop this 1
            push constant 42
            pop local 0
            push local 0
            pop this 0
            push pointer 0
            return            
            """;
        assertEquals(expected, actual);
    }

    @Test
    public void testMethodSimples() {
        var input = """
            class Main {
                function void main () {
                    var Point p1, p2;
                    var int d;
                    let d = p1.distance(p2);
                    return;
                }
            }      
         """;;
        var parser = new Parser(input.getBytes(StandardCharsets.UTF_8));
        parser.parse();
        String actual = parser.VMOutput();
        String expected = """
            function Main.main 3
            push local 0
            push local 1
            call Point.distance 2
            pop local 2
            push constant 0
            return
            """;
        assertEquals(expected, actual);
    }


    @Test
    public void testMethod() {
        var input = """
            class Point {
            field int x, y;
            static int pointCount;

            constructor Point new(int ax, int ay) {}

            method int getx() {}
            method int gety() {}
            method int getPointCount() {}

            method Point plus(Point other) {}
            method int distance(Point other) {
                var int dx, dy;
                let dx = x - other.getx();
                let dy = y - other.gety();
                return Math.sqrt((dx*dx) + (dy*dy));
            }
            method void print() {}
        }
         """;;
        var parser = new Parser(input.getBytes(StandardCharsets.UTF_8));
        parser.parse();
        String actual = parser.VMOutput();
        String expected = """
            function Point.new 0
            push constant 2
            call Memory.alloc 1
            pop pointer 0
            function Point.getx 0
            push argument 0
            pop pointer 0
            function Point.gety 0
            push argument 0
            pop pointer 0
            function Point.getPointCount 0
            push argument 0
            pop pointer 0
            function Point.plus 0
            push argument 0
            pop pointer 0
            function Point.distance 2
            push argument 0
            pop pointer 0
            push this 0
            push argument 1
            call Point.getx 1
            sub
            pop local 0
            push this 1
            push argument 1
            call Point.gety 1
            sub
            pop local 1
            push local 0
            push local 0
            call Math.multiply 2
            push local 1
            push local 1
            call Math.multiply 2
            add
            call Math.sqrt 1
            return
            function Point.print 0
            push argument 0
            pop pointer 0
            """;
        assertEquals(expected, actual);
    }

    @Test
    public void testFunction() {
        var input = """
            class Main {
                function int soma (int x, int y) {
                        var int d;
                        let d = x + y;
                        return  d;
                }

                function void main () {
                        var int d;
                        let d = Main.soma(4,5);
                        return;
                }
            }
         """;;
        var parser = new Parser(input.getBytes(StandardCharsets.UTF_8));
        parser.parse();
        String actual = parser.VMOutput();
        String expected = """
            function Main.soma 1
            push argument 0
            push argument 1
            add
            pop local 0
            push local 0
            return
            function Main.main 1
            push constant 4
            push constant 5
            call Main.soma 2
            pop local 0
            push constant 0
            return
            """;
        assertEquals(expected, actual);
    }

    @Test
    public void testInitArray() {
        var input = """
            class Main {
                function void main () {
                    var Array arr;
                    let arr = Array.new (5);
                    return;
                }
            }
         """;;
        var parser = new Parser(input.getBytes(StandardCharsets.UTF_8));
        parser.parse();
        String actual = parser.VMOutput();
        String expected = """
            function Main.main 1
            push constant 5
            call Array.new 1
            pop local 0
            push constant 0
            return
            """;
        assertEquals(expected, actual);
    }

    @Test
    public void testCalcArray() {
        var input = """
            class Main {
                function void main () {
                    var Array bar;
                    var int a;
                    let bar = Array.new(10);
                    let bar[1] = bar[1] * 19;
                    return;
                }
            }
         """;;
        var parser = new Parser(input.getBytes(StandardCharsets.UTF_8));
        parser.parse();
        String actual = parser.VMOutput();
        String expected = """
            function Main.main 2
            push constant 10
            call Array.new 1
            pop local 0
            push constant 1
            push local 0
            add
            push constant 1
            push local 0
            add
            pop pointer 1
            push that 0
            push constant 19
            call Math.multiply 2
            pop temp 0
            pop pointer 1
            push temp 0
            pop that 0
            push constant 0
            return
            """;
        assertEquals(expected, actual);
    }


    @Test
    public void testCalcArrays() {
        var input = """
            class Main {
                function void main () {
                    var Array a;
                    var Array b;
                    var int i , j, r;
                    let i = 1;
                    let j = 2;
                    let a = Array.new(1, 2);
                    let b = Array.new(2, 1);
                    let r = a[b[j]] + b[a[i]];
                    return;
                }
            }
         """;;
        var parser = new Parser(input.getBytes(StandardCharsets.UTF_8));
        parser.parse();
        String actual = parser.VMOutput();
        String expected = """
            function Main.main 5
            push constant 1
            pop local 2
            push constant 2
            pop local 3
            push constant 1
            push constant 2
            call Array.new 2
            pop local 0
            push constant 2
            push constant 1
            call Array.new 2
            pop local 1
            push local 3
            push local 1
            add
            pop pointer 1
            push that 0
            push local 0
            add
            pop pointer 1
            push that 0
            push local 2
            push local 0
            add
            pop pointer 1
            push that 0
            push local 1
            add
            pop pointer 1
            push that 0
            add
            pop local 4
            push constant 0
            return
            """;
        assertEquals(expected, actual);
    }

    

}
