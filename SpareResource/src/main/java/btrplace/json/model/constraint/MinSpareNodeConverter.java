package btrplace.json.model.constraint;

import btrplace.json.AbstractJSONObjectConverter;
import btrplace.json.JSONConverterException;
import btrplace.model.constraint.MinSpareNode;
import net.minidev.json.JSONObject;

/**
 * JSON Converter for the constraint {@link btrplace.json.model.constraint.MinSpareNodeConverter}.
 *
 * @author Tu Huynh Dang
 */
public class MinSpareNodeConverter extends SatConstraintConverter<MinSpareNode> {
    @Override
    public Class<MinSpareNode> getSupportedConstraint() {
        return MinSpareNode.class;
    }

    @Override
    public String getJSONId() {
        return "MinSpareNode";
    }

    @Override
    public MinSpareNode fromJSON(JSONObject in) throws JSONConverterException {
        checkId(in);
        return new MinSpareNode(AbstractJSONObjectConverter.requiredUUIDs(in, "nodes"),
                (int) AbstractJSONObjectConverter.requiredLong(in, "amount"),
                AbstractJSONObjectConverter.requiredBoolean(in, "continuous"));
    }

    @Override
    public JSONObject toJSON(MinSpareNode maxSN) throws JSONConverterException {
        JSONObject c = new JSONObject();
        c.put("id", getJSONId());
        c.put("nodes", AbstractJSONObjectConverter.uuidsToJSON(maxSN.getInvolvedNodes()));
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
