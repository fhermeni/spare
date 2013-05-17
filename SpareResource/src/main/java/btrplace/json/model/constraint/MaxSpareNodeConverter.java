package btrplace.json.model.constraint;

import btrplace.json.JSONConverterException;
import btrplace.json.JSONUtils;
import btrplace.model.constraint.MaxSpareNode;
import net.minidev.json.JSONObject;

/**
 * JSON Converter for the constraint {@link btrplace.json.model.constraint.MaxSpareNodeConverter}.
 *
 * @author Tu Huynh Dang
 */
public class MaxSpareNodeConverter extends SatConstraintConverter<MaxSpareNode> {
    @Override
    public Class<MaxSpareNode> getSupportedConstraint() {
        return MaxSpareNode.class;
    }

    @Override
    public String getJSONId() {
        return "maxSpareNode";
    }

    @Override
    public MaxSpareNode fromJSON(JSONObject in) throws JSONConverterException {
        checkId(in);
        return new MaxSpareNode(JSONUtils.requiredUUIDs(in, "nodes"),
                (int) JSONUtils.requiredLong(in, "amount"),
                JSONUtils.requiredBoolean(in, "continuous"));
    }

    @Override
    public JSONObject toJSON(MaxSpareNode maxSN) throws JSONConverterException {
        JSONObject c = new JSONObject();
        c.put("id", getJSONId());
        c.put("nodes", JSONUtils.toJSON(maxSN.getInvolvedNodes()));
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
