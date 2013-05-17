package btrplace.json.model.constraint;

import btrplace.json.JSONConverterException;
import btrplace.json.JSONUtils;
import btrplace.model.constraint.MinSpareResources;
import net.minidev.json.JSONObject;

/**
 * JSON Converter for the constraint {@link btrplace.json.model.constraint.MinSpareResourcesConverter}.
 *
 * @author Tu Huynh Dang
 */
public class MinSpareResourcesConverter extends SatConstraintConverter<MinSpareResources> {
    @Override
    public Class<MinSpareResources> getSupportedConstraint() {
        return MinSpareResources.class;
    }

    @Override
    public String getJSONId() {
        return "MinSpareResources";
    }

    @Override
    public MinSpareResources fromJSON(JSONObject in) throws JSONConverterException {
        checkId(in);
        return new MinSpareResources(JSONUtils.requiredUUIDs(in, "nodes"),
                JSONUtils.requiredString(in, "rcId"),
                (int) JSONUtils.requiredLong(in, "amount"),
                JSONUtils.requiredBoolean(in, "continuous"));
    }

    @Override
    public JSONObject toJSON(MinSpareResources maxSN) throws JSONConverterException {
        JSONObject c = new JSONObject();
        c.put("id", getJSONId());
        c.put("nodes", JSONUtils.toJSON(maxSN.getInvolvedNodes()));
        c.put("rcId", maxSN.getResource());
        c.put("amount", (long) maxSN.getAmount());
        c.put("continuous", maxSN.isContinuous());
        return c;
    }

    /**
     * Check if the JSON object can be converted using this converter.
     * For being convertible, the key 'id' must be equals to {@link #getJSONId()}.
     *
     * @param in the object to test
     * @throws btrplace.json.JSONConverterException
     *          if the object is not compatible
     */
    public void checkId(JSONObject in) throws JSONConverterException {
        Object id = in.get("id");
        if (id != null && !id.toString().equals(getJSONId())) {
            throw new JSONConverterException("Incorrect converter for " + in.toJSONString() + ". Expecting a constraint id '" + id + "'");
        }
    }
}
