package accsswap;

import ga2.data.AccessoryData;
import ga2.data.AmpedExternalizer;
import ga2.setting.GameSetting;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import kotori.io.Externalizer;
import kotori.io.ExternalizerOutputStream;
import kotori.io.KxrFile;

public final class GsIO {
    private GsIO() {
    }

    public static GameSetting load(String path) throws Exception {
        Externalizer.setDefault(AmpedExternalizer.getExternalizer());
        Method m = GameSetting.class.getDeclaredMethod("readKxr", String.class);
        m.setAccessible(true);
        return (GameSetting) m.invoke(null, path);
    }

    public static void copyFile(File src, File dst) throws Exception {
        FileChannel in = null;
        FileChannel out = null;
        try {
            in = new FileInputStream(src).getChannel();
            out = new FileOutputStream(dst).getChannel();
            out.transferFrom(in, 0, in.size());
        } finally {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        }
    }

    public static void writeKeepMeta(GameSetting gs, File outFile, File cleanFile) throws Exception {
        KxrFile clean = KxrFile.open(cleanFile.getAbsolutePath(), "pwpw", "r");
        List names = new ArrayList();
        List datas = new ArrayList();
        try {
            KxrFile.Entry[] list = clean.getRoot().getList();
            for (int i = 0; list != null && i < list.length; i++) {
                String name = list[i].getName();
                if ("gs".equals(name)) {
                    continue;
                }
                byte[] buf = new byte[list[i].size()];
                list[i].getData(buf);
                names.add(name);
                datas.add(buf);
            }
        } finally {
            clean.close();
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ExternalizerOutputStream eos = new ExternalizerOutputStream(bos, false);
        eos.writeObject(gs);
        eos.close();

        KxrFile out = KxrFile.openClean(outFile.getAbsolutePath(), "pwpw");
        try {
            out.create(out.getRoot(), "gs", bos.toByteArray(), 0);
            for (int i = 0; i < names.size(); i++) {
                out.create(out.getRoot(), (String) names.get(i), (byte[]) datas.get(i), 0);
            }
        } finally {
            out.close();
        }
    }

    public static AccsEntry[] indexAccs(GameSetting gs) {
        List list = new ArrayList();
        if (gs == null || gs.accs == null) {
            return new AccsEntry[0];
        }
        for (int i = 0; i < gs.accs.length; i++) {
            AccessoryData a = gs.accs[i];
            if (a == null || a.name == null) {
                continue;
            }
            int parts = a.parts == null ? 0 : a.parts.length;
            list.add(new AccsEntry(i, a.name, a.uniqname, a.privately, parts));
        }
        AccsEntry[] arr = new AccsEntry[list.size()];
        for (int i = 0; i < list.size(); i++) {
            arr[i] = (AccsEntry) list.get(i);
        }
        return arr;
    }
}
