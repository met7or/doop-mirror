package deepdoop.datalog;

import java.util.List;

public class Primitive extends Predicate {

	int _capacity;

	public Primitive(String name, String capacity) {
		super(name);
		_capacity = normalize(name, capacity);
	}

	// There is no notion of initializing a primitive type
	@Override
	public Predicate init(String id) {
		return this;
	}

	@Override
	public String getName() {
		return _name + (_capacity != 0 ? "[" + _capacity + "]" : "");
	}

	@Override
	public void setTypes(List<Predicate> types) {}

	@Override
	public String toString() {
		return _name + (_capacity != 0 ? "[" + _capacity + "]" : "") + "/1";
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
