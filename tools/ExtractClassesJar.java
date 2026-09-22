import java.io.File;
import java.io.FileOutputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import kotori.io.KxrFile;

public class ExtractClassesJar {
    static void walk(KxrFile.Entry e, String prefix, JarOutputStream jos, int[] n) throws Exception {
        if (e == null) {
            return;
        }
        String name = e.getName();
        String full = prefix.length() == 0 ? name : (prefix + "/" + name);
        KxrFile.Entry[] kids = e.getList();
        if (kids != null && kids.length > 0) {
            for (int i = 0; i < kids.length; i++) {
                walk(kids[i], full, jos, n);
            }
            return;
        }
        byte[] buf = new byte[e.size()];
        e.getData(buf);
        String entry = full.replace('\\', '/');
        if (entry.startsWith("/")) {
            entry = entry.substring(1);
        }
        jos.putNextEntry(new JarEntry(entry));
        jos.write(buf);
        jos.closeEntry();
        n[0]++;
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: ExtractClassesJar <classes.kxr> [out.jar]");
            System.exit(1);
        }
        String kxr = args[0];
        String jar = args.length > 1 ? args[1] : "lib/classes.jar";
        File out = new File(jar);
        if (out.getParentFile() != null) {
            out.getParentFile().mkdirs();
        }
        KxrFile k = KxrFile.open(kxr, "pwpw", "r");
        JarOutputStream jos = new JarOutputStream(new FileOutputStream(out));
        int[] n = new int[1];
        walk(k.getRoot(), "", jos, n);
        jos.close();
        k.close();
        System.out.println("OK entries=" + n[0] + " -> " + out.getAbsolutePath() + " (" + out.length() + " bytes)");
    }
}
