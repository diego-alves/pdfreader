package main;

import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by diegoalves on 07/10/15.
 */
public class Program {

    private static DateFormat df = new SimpleDateFormat("dd/MM/yyyy");

    public static void main(String[] args) throws IOException, COSVisitorException, ParseException {

        if (args.length == 0){
            System.out.print("faltou arquivo como argumento");
            System.exit(1);
            return;
        }

        File file = new File(args[0]);

        PDFParser parser = new PDFParser(new FileInputStream(file));
        parser.parse();
        COSDocument cosDoc = parser.getDocument();
        PDFTextStripper pdfStripper = new PDFTextStripper();
        PDDocument pdDoc = new PDDocument(cosDoc);
        //pdfStripper.setStartPage(1);
        //pdfStripper.setEndPage(5);
        String parsedText = pdfStripper.getText(pdDoc);

        //System.out.println(parsedText);

        String page = parsedText.substring(
                parsedText.indexOf("Página") + 14,
                parsedText.indexOf("APLICAÇÃO"));
        //System.out.println(page);

        String[] lines = page.split("\n");
        List<Map<String, String>> list = new ArrayList<>();

        Set<String> descs = new HashSet<>();
        for (int i = 0; i < lines.length; i+=3) {
            Map<String, String> map = new HashMap<>();

            map.put("date", lines[i].substring(0, 10));
            map.put("hour", lines[i].substring(11, 19)); //hour
            map.put("description", lines[i].substring(20, lines[i].length()) + lines[i+1]); //description

            String[] splited = lines[i+2].split(" ");
            map.put("type", splited[0]); // debito ou credito
            map.put("value", splited[1]); // valor
            map.put("code", splited[2]); // c�digo
            map.put("balance", splited[3]); // saldo

            list.add(map);
        }
        Map<Date, String[]> result = new TreeMap<>();
        for(Map<String, String> map : list){
            //System.out.println(map);
            if("D".equals(map.get("type"))) {
                descs.add(map.get("description"));
                addToMap(result, df.parse(map.get("date")), map.get("hour"), map.get("description"));
            }
        }

        for(Map.Entry<Date, String[]> entry : result.entrySet()){
            System.out.print(df.format(entry.getKey()));
            for (String s : entry.getValue()) {
                System.out.print("\t" + (s == null ? "        " : s));
            }
            System.out.println("");
        }

    }

    static void addToMap(Map<Date, String[]> map, Date key, String value, String description) {
        if(map.containsKey(key)) {
            map.get(key)[getIndex(description)] = value;
        } else {
            String[] array = new String[4];
            array[getIndex(description)] = value;
            map.put(key, array);
        }
    }

    private static int getIndex(String description) {
        if(description.contains("431") || description.contains("004")){
            if(description.contains("IDA"))
                return 3;
            else if(description.contains("VOLTA"))
                return 0;
        } else if (description.contains("FARIA LIMA")) {
            return 2;
        } else if (description.contains("SAC")){
            return 1;
        }
        throw new IllegalArgumentException(description);
    }
}