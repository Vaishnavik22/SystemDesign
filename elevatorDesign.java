// https://farenda.com/java-concurrency-tutorial/
// https://github.com/tssovi/grokking-the-object-oriented-design-interview
// https://www.baeldung.com/java-daemon-thread
enum Direction {UP, DOWN};
enum ElevatorState {MOVING, IDLE};
enum RequestType { EXTERNAL_REQUEST, INTERNAL_REQUEST};

class Request {
	RequestType requestType;
	int floor;

	public RequestType() {}

	public Request(RequestType requestType, int floor) {
		this.requestType = requestType;
		this.floor = floor;

	}

	public RequestType getRequestTpe() {
		return this.requestType;
	}

	public void setRequestType(RequestType requestType) {
		this.requestType = requestType;
	}

	public int getFloor() {
		return this.floor;
	}

	public void setFloor(int floow) {
		this.floor = floor;
	}
}

class ExternalRequest extends Request {
	Direction direction;

	public ExternalRequest() {}

	public ExternalRequest(RequestType requestType, int floor, Direction direction) {
		super(requestType, floor);
		this.direction = direction;
	}

	public Direction getDirection() {
		return this.direction;
	}

	public void setDirection(Direction direction) {
		this.direction = direction;
	}
}

class InternalRequest extends Request() {
	int elevatorNumber;

	public InternalRequest() {}

	public InternalRequest(RequestType requestType, int floor, int elevatorNumber) {
		super(requestType, floor);
		this.elevatorNumber = elevatorNumber;
	}

	public int getElevatorNumber() {
		return this.elevatorNumber;
	}

	public void setElevatorNumber(int elevatorNumber) {
		this.elevatorNumber = elevatorNumber;
	}
}

class Elevator {
	int currentFloor;
	Direction currentDirection;
	int elevatorNumber;
	int maxFloors = 10;

	LinkedList<Request> currentJobs;
	LinkedList<Request> pendingUpJobs;
	LinkedList<Request> pendingDownJobs;

	public Elevator() {}

	public Elevator(int currentFloor, Direction currentDirection, int elevatorNumber) {
		this.currentFloor = currentFloor;
		this.currentDirection = currentDirection;
		this.elevatorNumber = elevatorNumber;

		// TODO: figure out how to make these thread safe
		currentJobs = new LinkedList<>();
		pendingUpJobs = new LinkedList<>();
		// Sort it in descending order
		pendingDownJobs = new LinkedList<>((j1, j2) -> j2.floor - j1.floor);
	}

	public Direction getCurrentDirection() {
		return this.currentDirection;
	}

	public void setDirection(Direction direction) {
		this.currentDirection = direction;
	}

	public int getElevatorNumber() {
		return this.elevatorNumber;
	}

	public void setElevatorNumber(int elevatorNumber) {
		this.elevatorNumber = elevatorNumber;
	}

	public int getFloor() {
		return this.currentFloor;
	}

	public void setElevatorNumber(int floor) {
		this.currentFloor = floor;
	}

	public void start() {
		while (true) {
			if (currentJobs.size() > 0) {
				processCurrentJobs();
			} else if (currentDirection == Direction.DOWN) {
				if (pendingUpJobs.size() > 0) {
					LinkedList<Request> temp = currentJobs;
					currentJobs = pendingUpJobs;
					pendingUpJobs = currentJobs;
					currentDirection = Direction.UP;
				}
			} else if (currentDirection == Direction.UP) {
				if (pendingDownJobs.size() > 0) {
					LinkedList<Request> temp = currentJobs;
					currentJobs = pendingDownJobs;
					pendingDownJobs = currentJobs;
					currentDirection = Direction.DOWN;
				}
			}
		}
	}

	private void processCurrentJobs() {

		if (currentDirection == Direction.UP) {
			for (int startFloor = currentFloor; startFloor <= maxFloors && !currentJobs.isEmpty(); ++startFloor) {
				if (startFloor == currentJobs.peekFirst().getFloor()) {
					Request r = currentJobs.pollFirst();
					System.out.println("Exceuting request type: " + r.getRequestTpe() + " on floor  " + r.getFloor());
				}
			}
		} else {
			for (int startFloor = maxFloors; startFloor >= 0 && !currentJobs.isEmpty(); --startFloor) {
				if (startFloor == currentJobs.peekFirst().getFloor()) {
					Request r = currentJobs.pollFirst();
					System.out.println("Exceuting request type: " + r.getRequestTpe() + " on floor  " + r.getFloor());
				}
			}
		}
	}

	public void addRequest(Request request) {
		if (request.getDirection() == Direction.UP) {
			if (currentFloor < request.getFloor()) {
				currentJobs.add(request);
			} else {
				pendingUpJobs.add(request);
			}
		} else {
			if (currentFloor > request.getFloor()) {
				currentJobs.add(request);
			} else {
				pendingDownJobs.add(request);
			}
		}
	}
}

class ElevatorWorker implements Runnable {
	Elevator elevator;

	public ElevatorWorker(Elevator elevator) {
		this.elevator = elevator; 
	}

	public void run() {
		elevator.start();
	}
}

class Coordinator {
	public static void main(String args[]) {
		List<Thread> threads = new ArrayList<>();
		List<Elevator> elevators = new ArrayList<>();

		for (int i = 0; i < 5; ++i) {
			Elevator elevator = new Elevator();
			elevator.setElevatorNumber(i);
			ElevatorWorker worker = new ElevatorWorker(elevator);
			Thread thread = new Thread(worker);
			threads.add(thread);
		}

		// Add requests
		for (int i = 0; i < 10; ++i) {
			Elevator e = elevators.get(i % 5);
			e.addRequest(new ExternalRequest());
			e.addRequest(new InternalRequest());
		}

	}
}