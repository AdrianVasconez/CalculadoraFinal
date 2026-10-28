package com.example.calculadorafinal;


import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private TextView tvPantalla;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_calculadora);

        tvPantalla = findViewById(R.id.tvPantalla);

        // --- 1. CONFIGURACIÓN DE BOTONES NUMÉRICOS (0-9) ---
        int[] idsNumeros = {
                R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
                R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9
        };
        View.OnClickListener listenerNumeros = v -> {
            Button b = (Button) v;
            agregarNumero(b.getText().toString());
        };
        for (int id : idsNumeros) findViewById(id).setOnClickListener(listenerNumeros);

        // --- 2. CONFIGURACIÓN DE OPERADORES (+, -, *, /) ---
        int[] idsOperadores = {R.id.btnSuma, R.id.btnResta, R.id.btnMult, R.id.btnDiv};
        View.OnClickListener listenerOperadores = v -> {
            Button b = (Button) v;
            agregarOperador(b.getText().toString());
        };
        for (int id : idsOperadores) findViewById(id).setOnClickListener(listenerOperadores);

        // --- 3. CONFIGURACIÓN DE BOTONES ESPECIALES ---
        findViewById(R.id.btnPunto).setOnClickListener(v -> agregarComa());

        // Criterio 7: Botón AC (Limpiar todo)
        findViewById(R.id.btnAC).setOnClickListener(v -> tvPantalla.setText("0"));

        // Criterio 6: Botón C (Borrar el último carácter)
        findViewById(R.id.btnC).setOnClickListener(v -> borrarUltimo());

        // Criterio 8: Botón = (Calcular resultado)
        findViewById(R.id.btnIgual).setOnClickListener(v -> calcularResultado());
    }

    // Criterio 2 y 5: Funcionalidad numérica y bloqueo de ceros iniciales
    private void agregarNumero(String num) {
        String actual = tvPantalla.getText().toString();

        if (actual.equals("0")) {
            // Si solo hay un 0 en pantalla, lo reemplazamos por el nuevo número
            tvPantalla.setText(num);
        } else {
            // Separamos por operadores matemáticos para ver cuál es el último número que estamos escribiendo
            String[] partes = actual.split("[+\\-*/]");
            String ultimoNumero = partes.length > 0 ? partes[partes.length - 1] : "";

            // Evitamos ceros adicionales a la izquierda (Ej: "5+0" + "5" -> "5+5", evitando "5+05")
            if (ultimoNumero.equals("0") && actual.endsWith("0")) {
                tvPantalla.setText(actual.substring(0, actual.length() - 1) + num);
            } else {
                tvPantalla.setText(actual + num);
            }
        }
    }

    // Criterio 3: Operaciones concatenadas
    private void agregarOperador(String op) {
        String actual = tvPantalla.getText().toString();
        char ultimo = actual.charAt(actual.length() - 1);

        // Si el último carácter ya es un operador, lo reemplazamos (Ej: 5+ y pulsas - -> 5-)
        if (ultimo == '+' || ultimo == '-' || ultimo == '*' || ultimo == '/') {
            tvPantalla.setText(actual.substring(0, actual.length() - 1) + op);
        } else if (ultimo != '.') {
            // Concatenamos el operador (evitamos añadir operador justo después de un punto)
            tvPantalla.setText(actual + op);
        }
    }

    // Criterio 4: Control de coma decimal
    private void agregarComa() {
        String actual = tvPantalla.getText().toString();

        // Partimos la expresión por operadores para evaluar solo el número actual
        String[] partes = actual.split("[+\\-*/]");
        String ultimoNumero = partes.length > 0 ? partes[partes.length - 1] : "";

        // Solo añadimos el punto si el número que estamos escribiendo NO contiene ya uno
        if (!ultimoNumero.contains(".")) {
            tvPantalla.setText(actual + ".");
        }
    }

    // Criterio 6: Botón C (Borrar último carácter)
    private void borrarUltimo() {
        String actual = tvPantalla.getText().toString();
        if (actual.length() > 1) {
            tvPantalla.setText(actual.substring(0, actual.length() - 1));
        } else {
            // Si solo queda un carácter, devolvemos la pantalla a "0"
            tvPantalla.setText("0");
        }
    }

    // Criterio 8: Evaluar matemáticamente el resultado
    private void calcularResultado() {
        String expresion = tvPantalla.getText().toString();
        try {
            double resultado = evaluadorMatematico(expresion);

            // Si el resultado es un número entero (ej: 5.0), mostramos "5" sin decimales
            if (resultado == (long) resultado) {
                tvPantalla.setText(String.valueOf((long) resultado));
            } else {
                tvPantalla.setText(String.valueOf(resultado));
            }
        } catch (Exception e) {
            tvPantalla.setText("Error");
        }
    }

    // Algoritmo analizador clásico (Parser) para resolver strings matemáticos respetando jerarquía de operaciones
    private double evaluadorMatematico(final String str) {
        return new Object() {
            int pos = -1, ch;

            void nextChar() { ch = (++pos < str.length()) ? str.charAt(pos) : -1; }
            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) { nextChar(); return true; }
                return false;
            }

            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < str.length()) throw new RuntimeException("Unexpected: " + (char)ch);
                return x;
            }

            double parseExpression() {
                double x = parseTerm();
                for (;;) {
                    if (eat('+')) x += parseTerm();       // Suma
                    else if (eat('-')) x -= parseTerm();  // Resta
                    else return x;
                }
            }

            double parseTerm() {
                double x = parseFactor();
                for (;;) {
                    if (eat('*')) x *= parseFactor();     // Multiplicación
                    else if (eat('/')) x /= parseFactor(); // División
                    else return x;
                }
            }

            double parseFactor() {
                if (eat('+')) return parseFactor();
                if (eat('-')) return -parseFactor();

                double x;
                int startPos = this.pos;
                if ((ch >= '0' && ch <= '9') || ch == '.') {
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Double.parseDouble(str.substring(startPos, this.pos));
                } else {
                    throw new RuntimeException("Unexpected: " + (char)ch);
                }
                return x;
            }
        }.parse();
    }
}