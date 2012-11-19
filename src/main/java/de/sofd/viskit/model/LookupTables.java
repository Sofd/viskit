package de.sofd.viskit.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map.Entry;

import de.sofd.viskit.glutil.control.LutController;
import de.sofd.viskit.util.LutFunction;

public class LookupTables {

    static {
        try {
            LutController.init("img/luts/osx");
            LutController.loadFiles();
            LutController.init("img/luts");
            LutController.loadFiles();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Collection<LookupTable> getAllKnownLuts() {
        Collection<LookupTable> result = new ArrayList<LookupTable>();
        for (Entry<String, LutFunction> ent : LutController.getLutMap().entrySet()) {
            result.add(new LookupTableImpl(ent.getKey(), ent.getValue().getBuffer()));
        }
        return result;
    }

    public static LookupTable getLut(String name) {
        for (Entry<String, LutFunction> ent : LutController.getLutMap().entrySet()) {
            if (ent.getKey().equals(name)) {
                return (new LookupTableImpl(ent.getKey(), ent.getValue().getBuffer()));
            }
        }
        return null;
    }
}
