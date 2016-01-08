package de.intevation.lada.test.land;

import java.net.URL;
import java.util.Arrays;
import java.util.List;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import org.junit.Assert;

import de.intevation.lada.Protocol;
import de.intevation.lada.test.ServiceTest;

public class MesswertTest extends ServiceTest {

    private JsonObject expectedById;
    private JsonObject create;

    /**
     * @return The test protocol
     */
    public List<Protocol> getProtocol() {
        return protocol;
    }

    @Override
    public void init(
        URL baseUrl,
        List<Protocol> protocol
    ) {
        super.init(baseUrl, protocol);
        // Attributes with timestamps
        timestampAttributes = Arrays.asList(new String[]{
            "letzteAenderung",
            "treeModified"
        });

        // Prepare expected probe object
        JsonObject content = readJsonResource("/datasets/dbUnit_messwert.json");
        JsonObject messung = content.getJsonArray("land.messwert").getJsonObject(0);
        JsonObjectBuilder builder = convertObject(messung);
        builder.add("parentModified", 1450371851654L);
        builder.add("readonly", JsonValue.FALSE);
        builder.add("owner", JsonValue.TRUE);
        expectedById = builder.build();
        Assert.assertNotNull(expectedById);

        // Load probe object to test POST request
        create = readJsonResource("/datasets/messwert.json");
        Assert.assertNotNull(create);
    }

    public final void execute() {
        getAll("messwert", "rest/messwert?messungsId=1000");
        getById("messwert", "rest/messwert/10000", expectedById);
        JsonObject created = create("messwert", "rest/messwert", create);
        update("messwert", "rest/messwert/10000", "messwertNwg", "<", ">");
        delete("messwert", "rest/messwert/" + created.getJsonObject("data").get("id"));
    }
}
