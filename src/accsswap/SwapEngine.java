package accsswap;

import ga2.data.AccessoryData;
import ga2.setting.GameSetting;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import kotori.geom.Matrix;

public final class SwapEngine {
    public static final String BACKUP_NAME = "gs.accs_swap_clean.kxr";
    public static final String STATE_NAME = "accs_swap_state.txt";

    private SwapEngine() {
    }

    public static File backupFile(File gsFile) {
        return new File(gsFile.getParentFile(), BACKUP_NAME);
    }

    public static File stateFile(File gsFile) {
        return new File(gsFile.getParentFile(), STATE_NAME);
    }

    public static void ensureBackup(File gsFile) throws Exception {
        File bak = backupFile(gsFile);
        if (!bak.exists()) {
            GsIO.copyFile(gsFile, bak);
        }
    }

    public static String readState(File gsFile) {
        File f = stateFile(gsFile);
        if (!f.exists()) {
            return "";
        }
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
            try {
                String line = br.readLine();
                return line == null ? "" : line.trim();
            } finally {
                br.close();
            }
        } catch (Exception e) {
            return "";
        }
    }

    public static void writeState(File gsFile, String text) throws Exception {
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(stateFile(gsFile)), "UTF-8"));
        try {
            bw.write(text == null ? "" : text);
            bw.newLine();
        } finally {
            bw.close();
        }
    }

    public static AccessoryData.Parts copyPart(AccessoryData src, int index) {
        AccessoryData.Parts p = (AccessoryData.Parts) src.parts[index].clone();
        if (src.parts[index].mat != null) {
            p.mat = new Matrix(src.parts[index].mat);
        }
        return p;
    }

    public static AccessoryData.Parts[] copyAllParts(AccessoryData src) {
        if (src == null || src.parts == null || src.parts.length == 0) {
            throw new IllegalArgumentException("source has no parts");
        }
        AccessoryData.Parts[] arr = new AccessoryData.Parts[src.parts.length];
        for (int i = 0; i < src.parts.length; i++) {
            arr[i] = copyPart(src, i);
        }
        return arr;
    }

    public static void applySwap(File gsFile, int wearId, int lookId, boolean keepName) throws Exception {
        if (wearId == lookId) {
            throw new IllegalArgumentException("wear and look id are the same");
        }
        ensureBackup(gsFile);
        File bak = backupFile(gsFile);
        GameSetting clean = GsIO.load(bak.getAbsolutePath());
        int ver = clean.version;
        if (ver < 0) {
            throw new IllegalStateException("invalid version in backup");
        }
        if (wearId < 0 || wearId >= clean.accs.length || clean.accs[wearId] == null) {
            throw new IllegalArgumentException("wear accs not found: " + wearId);
        }
        if (lookId < 0 || lookId >= clean.accs.length || clean.accs[lookId] == null) {
            throw new IllegalArgumentException("look accs not found: " + lookId);
        }
        if (clean.accs[lookId].parts == null || clean.accs[lookId].parts.length == 0) {
            throw new IllegalArgumentException("look accs has no parts: " + lookId);
        }

        GameSetting out = GsIO.load(bak.getAbsolutePath());
        AccessoryData base = clean.accs[wearId];
        AccessoryData look = clean.accs[lookId];
        AccessoryData dst = (AccessoryData) base.clone();
        dst.parts = copyAllParts(look);
        if (!keepName) {
            dst.name = look.name;
            dst.uniqname = look.uniqname;
        }
        String prefix = "【外观来自 " + lookId + " " + look.name + "】";
        dst.desc = prefix + (base.desc == null ? "" : base.desc);
        out.accs[wearId] = dst;
        out.version = ver;
        GsIO.writeKeepMeta(out, gsFile, bak);

        GameSetting check = GsIO.load(gsFile.getAbsolutePath());
        if (check.version != ver) {
            GsIO.copyFile(bak, gsFile);
            throw new IllegalStateException("version lost after write: " + check.version);
        }
        writeState(gsFile, wearId + "<-" + lookId);
    }

    public static void restore(File gsFile) throws Exception {
        File bak = backupFile(gsFile);
        if (!bak.exists()) {
            throw new IllegalStateException("backup missing: " + bak.getAbsolutePath());
        }
        GsIO.copyFile(bak, gsFile);
        writeState(gsFile, "OFF");
    }

    public static List search(AccsEntry[] all, String query, int limit) {
        List result = new ArrayList();
        if (all == null || query == null) {
            return result;
        }
        String q = query.trim();
        if (q.length() == 0) {
            return result;
        }
        for (int i = 0; i < all.length; i++) {
            if (all[i].matches(q)) {
                result.add(all[i]);
                if (result.size() >= limit) {
                    break;
                }
            }
        }
        return result;
    }

    public static AccsEntry resolve(AccsEntry[] all, String text) {
        if (text == null) {
            return null;
        }
        String t = text.trim();
        if (t.length() == 0) {
            return null;
        }
        int colon = t.indexOf(' ');
        String head = colon > 0 ? t.substring(0, colon).trim() : t;
        if (head.matches("\\d+")) {
            int id = Integer.parseInt(head);
            for (int i = 0; i < all.length; i++) {
                if (all[i].id == id) {
                    return all[i];
                }
            }
            return null;
        }
        List hits = search(all, t, 20);
        if (hits.size() == 1) {
            return (AccsEntry) hits.get(0);
        }
        for (int i = 0; i < hits.size(); i++) {
            AccsEntry e = (AccsEntry) hits.get(i);
            if (e.name.equals(t) || e.display().equals(t)) {
                return e;
            }
        }
        if (hits.size() > 0) {
            return (AccsEntry) hits.get(0);
        }
        return null;
    }
}
