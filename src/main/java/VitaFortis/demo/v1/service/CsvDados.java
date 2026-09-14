package VitaFortis.demo.v1.service;

import java.util.ArrayList;
import java.util.List;

/** Leitura dos arquivos de catálogo e dos relatórios, incluindo campos entre aspas. */
final class CsvDados {
    private CsvDados() {}

    static List<List<String>> ler(String texto, char separador) {
        List<List<String>> linhas = new ArrayList<>();
        List<String> linha = new ArrayList<>();
        StringBuilder campo = new StringBuilder();
        boolean aspas = false;
        for (int i = 0; i < texto.length(); i++) {
            char c = texto.charAt(i);
            if (c == '"') {
                if (aspas && i + 1 < texto.length() && texto.charAt(i + 1) == '"') { campo.append('"'); i++; }
                else aspas = !aspas;
            } else if (c == separador && !aspas) {
                linha.add(campo.toString()); campo.setLength(0);
            } else if ((c == '\n' || c == '\r') && !aspas) {
                linha.add(campo.toString()); campo.setLength(0);
                linhas.add(linha); linha = new ArrayList<>();
                if (c == '\r' && i + 1 < texto.length() && texto.charAt(i + 1) == '\n') i++;
            } else campo.append(c);
        }
        if (aspas) throw new IllegalArgumentException("CSV com aspas nao fechadas");
        if (!linha.isEmpty() || !campo.isEmpty()) { linha.add(campo.toString()); linhas.add(linha); }
        return linhas;
    }
}
