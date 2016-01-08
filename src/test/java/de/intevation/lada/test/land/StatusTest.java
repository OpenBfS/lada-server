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

public class StatusTest extends ServiceTest {

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
            "datum",
            "treeModified"
        });

        // Prepare expected object
        JsonObject content = readJsonResource("/datasets/dbUnit_messung.json");
        JsonObject status = content.getJsonArray("land.status_protokoll").getJsonObject(0);
        JsonObjectBuilder builder = convertObject(status);
        builder.add("parentModified", 1450371851654L);
        builder.add("readonly", JsonValue.FALSE);
        builder.add("owner", JsonValue.TRUE);
        expectedById = builder.build();
        Assert.assertNotNull(expectedById);

        // Load object to test POST request
        create = readJsonResource("/datasets/status.json");
        Assert.assertNotNull(create);
    }

    public final void execute() {
        getAll("status", "rest/status?messungsId=1000");
        getById("status", "rest/status/1000", expectedById);
        //update("status", "rest/status/1000", "text", "Initialer Status", "Initialer Status geändert");
        //JsonObject created =
        create("status", "rest/status", create);
        //delete("status", "rest/status/" + created.getJsonObject("data").get("id"));
    }

}
