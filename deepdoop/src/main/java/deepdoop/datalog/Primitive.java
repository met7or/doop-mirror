package deepdoop.datalog;

import java.util.Arrays;
import java.util.List;

public class Primitive implements IAtom {

	String       _name;
	int          _capacity;
	VariableExpr _var;

	public Primitive(String name, String capacity, VariableExpr var) {
		_capacity = normalize(name, capacity);
		_name     = name + (_capacity != 0 ? "[" + _capacity + "]" : "");
		_var      = var;
	}

	@Override
	public String name() {
		return _name;
	}

	@Override
	public String stage() { return null; }

	@Override
	public int arity() {
		return 1;
	}

	@Override
	public List<IExpr> getExprs() {
		return Arrays.asList(_var);
	}

	@Override
	public List<VariableExpr> getExprsAsVars() {
		return Arrays.asList(_var);
	}

	@Override
	public Primitive init(Initializer ini) {
		return this;
	}

	@Override
	public String toString() {
		return _name + "(" + _var + ")";
	}


	static int normalize(String name, String capacity) {
		switch (name) {
			case "uint":
			case "int":
			case "float":
			case "decimal":
				// capacity as a string is wrapped in square brackets
				return capacity == null ? 64 : Integer.parseInt(capacity.substring(1).substring(0, capacity.length()-2));
			default:
				return 0;
		}
	}
}
