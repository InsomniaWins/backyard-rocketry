package wins.insomnia.backyardrocketry.util.update;

public record UpdateListenerInstruction(InstructionType instructionType, IGenericUpdateListener listener) {

	enum InstructionType {
		REGISTER_LISTENER,
		REGISTER_FIXED_LISTENER,
		UNREGISTER_LISTENER,
		UNREGISTER_FIXED_LISTENER
	}


}
