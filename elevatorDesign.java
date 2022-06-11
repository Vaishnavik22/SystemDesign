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

	public RequestType getRequestType() {
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

	public ExternalRequest(int floor, Direction direction) {
		super(RequestType.EXTERNAL_REQUEST, floor);
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

	public InternalRequest(int floor, int elevatorNumber) {
		super(RequestType.INTERNAL_REQUEST, floor);
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
	int maxFloors;

	ConcurrentLinkedQueue<Request> currentJobs;
	ConcurrentLinkedQueue<Request> pendingUpJobs;
	ConcurrentLinkedQueue<Request> pendingDownJobs;

	public Elevator() {}

	public Elevator(int elevatorNumber, int maxFloors) {
		this.currentFloor = 0;
		this.currentDirection = Direction.UP;
		this.elevatorNumber = elevatorNumber;
		this.maxFloors = maxFloors;

		currentJobs = new ConcurrentLinkedQueue<>();
		pendingUpJobs = new ConcurrentLinkedQueue<>();
		// Sort it in descending order
		pendingDownJobs = new ConcurrentLinkedQueue<>((j1, j2) -> j2.floor - j1.floor);
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
					currentJobs.addAll(pendingUpJobs);
					pendingUpJobs.clear();
					currentDirection = Direction.UP;
				}
			} else if (currentDirection == Direction.UP) {
				if (pendingDownJobs.size() > 0) {
					currentJobs.addAll(pendingDownJobs);
					pendingDownJobs.clear();
					currentDirection = Direction.DOWN;
				}
			}
		}
	}

	private void processCurrentJobs() {
		int startFloor = currentJobs.peekFirst().getFloor();
		currentFloor = startFloor;
		if (currentDirection == Direction.UP) {
			for (; startFloor <= maxFloors && !currentJobs.isEmpty(); ++startFloor) {
				if (startFloor == currentJobs.peekFirst().getFloor()) {
					Request r = currentJobs.pollFirst();
					System.out.println("Moving Up. Exceuting request type: " + r.getRequestType() + " on floor  " + r.getFloor());
				}
			}
		} else {
			for (; startFloor >= 0 && !currentJobs.isEmpty(); --startFloor) {
				if (startFloor == currentJobs.peekFirst().getFloor()) {
					Request r = currentJobs.pollFirst();
					System.out.println("Moving down. Exceuting request type: " + r.getRequestType() + " on floor  " + r.getFloor());
				}
			}
		}
	}

	public void addRequest(Request request) {
		if (request.getDirection() == Direction.UP) {
			if (currentFloor < request.getFloor() && currentDirection == Direction.UP) {
				currentJobs.add(request);
			} else {
				pendingUpJobs.add(request);
			}
		} else {
			if (currentFloor > request.getFloor() && currentDirection == Direction.DOWN) {
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

class AddRequestsWorker implements Runnable {
	Elevator elevator;
	Request request;

	public AddRequestsWorker(Elevator elevator, Request request) {
		this.elevator = elevator;
		this.request = request;
	}

	public void run() {
		elevator.addRequest(request);
	}
}

class Coordinator {
	LinkedList<Elevator> elevators;
	int elevatorCount;
	ExecutorService elevatorExecutor;
	ExecutorService addRequestExecutor;
	Queue<Request> requestsToProcess;
	int nextElevatorToSendRequest;
	public final maxFloors = 10;

	public Coordinator(int elevatorCount) {
		this.elevatorCount = elevatorCount;
		elevators = new LinkedList<>();
		elevatorExecutor = Executors.newFixedThreadPool(elevatorCount);
		addRequestExecutor = Executors.newFixedThreadPool(elevatorCount);
		requestsToProcess = new LinkedList<>();
		nextElevatorToSendRequest = 1;

		for (int i = 0; i < elevatorCount; ++i) {
			Elevator elevator = new Elevator(i, maxFloors);
			ElevatorWorker worker = new ElevatorWorker(elevator);
			elevatorExecutor.execute(worker);
		}
	}

	public static void main(String args[]) {
		processRequests();
	}

	private void processRequests() {
		while (true) {
			if (!requestsToProcess.isEmpty()) {
				Request r = requestsToProcess.poll();

				if (r.getRequestType() == RequestType.INTERNAL_REQUEST) {
					addRequestExecutor.execute(new AddRequestsWorker(elevators.get(r.getElevatorNumber()), r));
				} else {
					// Send requests in a round robin manner
					addRequestExecutor.execute(new AddRequestsWorker(elevators.get(nextElevatorToSendRequest), r));
					nextElevatorToSendRequest = (nextElevatorToSendRequest + 1) % elevatorCount;
				}
			}
		}
	}
}