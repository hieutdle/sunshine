package de.hpi.swa.lox.test.challenge;

import org.junit.Test;

import de.hpi.swa.lox.test.AbstractLoxTest;

public class ControlFlowTest extends AbstractLoxTest {
    @Test
    public void testWhileLoopBreak() {
        runAndExpect("while loop with break",
                "var i = 0; while (true) { i = i + 1; if (i > 100) break; } print i;",
                "101\n");
    }

    @Test
    public void testForLoopWithBreak() {
        runAndExpect("for loop with break",
                "for(var i = 0; i < 100; i = i + 1) { if (i == 50) break; print i; }",
                "0\n1\n2\n3\n4\n5\n6\n7\n8\n9\n10\n11\n12\n13\n14\n15\n16\n17\n18\n19\n20\n21\n22\n23\n24\n25\n26\n27\n28\n29\n30\n31\n32\n33\n34\n35\n36\n37\n38\n39\n40\n41\n42\n43\n44\n45\n46\n47\n48\n49\n");
    }

    // @Test
    // public void testWhileLoopWithContinue() {
    //     runAndExpect("while loop with continue",
    //             "var i = 0; var sum = 0; while (true) { i = i + 1; if (i % 2 == 0) continue; sum = sum + i; if (i > 100) break; } print sum;",
    //             "2601\n");
    // }

    // @Test
    // public void testForLoopWithContinue() {
    //     runAndExpect("for loop with continue",
    //             "for(var i=0; i < 100; i = i + 1) { if (i > 10 and i < 20) continue; print i; }",
    //             "0\n1\n2\n3\n4\n5\n6\n7\n8\n9\n10\n20\n21\n22\n23\n24\n25\n26\n27\n28\n29\n30\n31\n32\n33\n34\n35\n36\n37\n38\n39\n40\n41\n42\n43\n44\n45\n46\n47\n48\n49\n50\n51\n52\n53\n54\n55\n56\n57\n58\n59\n60\n61\n62\n63\n64\n65\n66\n67\n68\n69\n70\n71\n72\n73\n74\n75\n76\n77\n78\n79\n80\n81\n82\n83\n84\n85\n86\n87\n88\n89\n90\n91\n92\n93\n94\n95\n96\n97\n98\n99\n");
    // }

}
