package btrplace.json.model.constraint;

import btrplace.json.AbstractJSONObjectConverter;
import btrplace.json.JSONConverterException;
import btrplace.model.constraint.MaxOnlines;
import net.minidev.json.JSONObject;

/**
 * JSON Converter for the constraint {@link MaxOnlinesConverter}.
 *
 * @author Tu Huynh Dang
 */
public class MaxOnlinesConverter extends SatConstraintConverter<MaxOnlines> {
    @Override
    public Class<MaxOnlines> getSupportedConstraint() {
        return MaxOnlines.class;
    }

    @Override
    public String getJSONId() {
        return "maxOnlines";
    }

    @Override
    public MaxOnlines fromJSON(JSONObject in) throws JSONConverterException {
        checkId(in);
        return new MaxOnlines(AbstractJSONObjectConverter.requiredUUIDs(in, "nodes"),
                (int) AbstractJSONObjectConverter.requiredLong(in, "amount"),
                AbstractJSONObjectConverter.requiredBoolean(in, "continuous"));
    }

    @Override
    public JSONObject toJSON(MaxOnlines maxOnlines) throws JSONConverterException {
        JSONObject c = new JSONObject();
        c.put("id", getJSONId());
        c.put("nodes", AbstractJSONObjectConverter.uuidsToJSON(maxOnlines.getInvolvedNodes()));
        c.put("amount", (long) maxOnlines.getAmount());
        c.put("continuous", maxOnlines.isContinuous());
        return c;
    }

    /**
     * Check if the JSON object can be converted using this converter.
     * For being convertible, the key 'id' must be equals to {@link #getJSONId()}.
     *
     * @param in the object to test
     * @throws JSONConverterException if the object is not compatible
     */
    public void checkId(JSONObject in) throws JSONConverterException {
        Object id = in.get("id");
        if (id != null && !id.toString().equals(getJSONId())) {
            throw new JSONConverterException("Incorrect converter for " + in.toJSONString() + ". Expecting a constraint id '" + id + "'");
        }
    }
}
