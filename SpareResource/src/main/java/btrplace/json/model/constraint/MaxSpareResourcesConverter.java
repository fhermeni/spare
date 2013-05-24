package btrplace.json.model.constraint;

import btrplace.json.AbstractJSONObjectConverter;
import btrplace.json.JSONConverterException;
import btrplace.model.constraint.MaxSpareResources;
import net.minidev.json.JSONObject;

/**
 * JSON Converter for the constraint {@link btrplace.json.model.constraint.MaxSpareResourcesConverter}.
 *
 * @author Tu Huynh Dang
 */
public class MaxSpareResourcesConverter extends SatConstraintConverter<MaxSpareResources> {
    @Override
    public Class<MaxSpareResources> getSupportedConstraint() {
        return MaxSpareResources.class;
    }

    @Override
    public String getJSONId() {
        return "MaxSpareResources";
    }

    @Override
    public MaxSpareResources fromJSON(JSONObject in) throws JSONConverterException {
        checkId(in);
        return new MaxSpareResources(AbstractJSONObjectConverter.requiredUUIDs(in, "nodes"),
                AbstractJSONObjectConverter.requiredString(in, "rcId"),
                (int) AbstractJSONObjectConverter.requiredLong(in, "amount"),
                AbstractJSONObjectConverter.requiredBoolean(in, "continuous"));
    }

    @Override
    public JSONObject toJSON(MaxSpareResources maxSN) throws JSONConverterException {
        JSONObject c = new JSONObject();
        c.put("id", getJSONId());
        c.put("nodes", AbstractJSONObjectConverter.uuidsToJSON(maxSN.getInvolvedNodes()));
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
