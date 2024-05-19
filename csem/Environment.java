package csem;

import java.util.HashMap;
import java.util.Map;

import parser.ASTNode;

public class Environment {
    private Environment parent;
    private Map<String, ASTNode> nameValueMap;

    public Environment() {
        nameValueMap = new HashMap<>();
    }

    public Environment getParent() {
        return parent;
    }

    public void setParent(Environment parent) {
        this.parent = parent;
    }

    /**
     * Looks up the binding of the given key in this Environment's mappings
     * or in the inheritance hierarchy, starting from the current Environment.
     * 
     * @param key The key of the mapping to find.
     * @return The ASTNode that corresponds to the mapping of the key passed in as
     *         an argument,
     *         or null if no mapping was found.
     */
    public ASTNode lookup(String key) {
        ASTNode returnValue = nameValueMap.get(key);

        if (returnValue != null) {
            return returnValue.accept(new NodeCopier());
        }

        if (parent != null) {
            return parent.lookup(key);
        } else {
            return null;
        }
    }

    /**
     * The addMapping function adds a key-value pair to a map.
     * 
     * @param key   The key is a string that represents the identifier or name of
     *              the mapping. It is used
     *              to uniquely identify the mapping in the map.
     * @param value The value parameter is of type ASTNode, which represents an
     *              abstract syntax tree
     *              node. It is used to store the value associated with the given
     *              key in the nameValueMap.
     */
    public void addMapping(String key, ASTNode value) {
        nameValueMap.put(key, value);
    }
}
