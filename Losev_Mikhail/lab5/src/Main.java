
import AhoCorasick.AhoCorasick;
import java.util.Scanner;
import javafx.util.Pair;
import java.util.StringTokenizer;

class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);


        String haystack = scanner.nextLine();
        int n = Integer.parseInt(scanner.nextLine());   // better than nextInt(), skips the rest of the line
        String[] needles = new String[n];
        for (int i = 0; i < n; i++) {
            needles[i] = scanner.nextLine();
        }


        //AhoCorasick test #1:
        AhoCorasick ahoCorasick = new AhoCorasick(needles, haystack);

        while (ahoCorasick.hasMoreElements()) {
            Pair <Integer, Integer> occurrence = ahoCorasick.nextOccurrence();
            System.out.println((occurrence.getKey() + 1) + " " + (occurrence.getValue() + 1));
        }



        //AhoCorasick test #2:
        /*
        AhoCorasick ahoCorasick = new AhoCorasick(needles);
        ahoCorasick.run(haystack);
        while (ahoCorasick.hasMoreElements()) {
            Pair <Integer, Integer> occurrence = ahoCorasick.nextOccurrence();
            System.out.println((occurrence.getKey() + 1) + " " + (occurrence.getValue() + 1));
        }
        */


        //JOKER TEST:
        /*
        String haystack = scanner.nextLine();
        String needle =  scanner.nextLine();
        char joker = scanner.nextLine().toCharArray()[0];   // input the joker

        searchWithJoker(needle, haystack, joker);
         */
    }

    private static void searchWithJoker(String needle, String haystack, char joker) {
        StringTokenizer st = new StringTokenizer(needle, "" + joker);
        String[] needles = new String[st.countTokens()];
        int[] startPositions = new int[st.countTokens()];
        int j = 0, behind = 0;
        while (st.hasMoreTokens()) {
            needles[j] = st.nextToken();
            startPositions[j] = needle.indexOf(needles[j], behind);
            behind = startPositions[j] + needles[j].length();
            j++;
        }


        AhoCorasick ahoCorasick = new AhoCorasick(needles, haystack);
        int[] countOccurances = new int[haystack.length()];
        while (ahoCorasick.hasMoreElements()) {
            Pair <Integer, Integer> occurrence = ahoCorasick.nextOccurrence();

            int pos = occurrence.getKey();
            int currWordNum = occurrence.getValue();

            if (pos - startPositions[currWordNum] < 0) continue;
            countOccurances[pos- startPositions[currWordNum]]++;
            if (countOccurances[pos - startPositions[currWordNum]] == needles.length  &&
                    pos - startPositions[currWordNum] <= haystack.length() - needle.length())
                System.out.println(pos - startPositions[currWordNum] + 1);
        }
    }
}
