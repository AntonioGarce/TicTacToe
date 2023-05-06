#include <cstring>
#include <iostream> // input output
#include <string> // strlen string
#include <sys/socket.h>
#include <arpa/inet.h>
#include <unistd.h>
#include <pthread.h> // multi threading
#include <errno.h>
#include <time.h>
#include <iomanip>
#include <vector>
#include <algorithm>

// server address
#define ADDRESS "127.0.0.1"
//#define PORT 8888
#define CONCURRENT_CONNECTION 10
#define QUEUE_CONNECTION 20
#define BUFFER_SIZE 1024
#define THREAD_STACK_SIZE 65536


std::vector<int> id_connects;

// current connections
int connection = 0;

// connection handler function
void *connection_handler(void*);


int main(int argc, char *argv[]){

	if(argc != 2){
		std::cerr << "Usage: port" << std::endl;
		exit(0);
	}

	//grab the port number
	int port = atoi(argv[1]);


	// thread identifier
	pthread_t thread_id;
	// thread attribute
	pthread_attr_t attr;

	if (pthread_attr_init(&attr) != 0){
		std::cout << "[ERROR][THREAD][INIT] " << strerror(errno) << "\n";
		return -1;
	}

	// stack size 1MB
	if (pthread_attr_setstacksize(&attr, THREAD_STACK_SIZE) != 0){
		std::cout << "[ERROR][THREAD][STACK] " << strerror(errno) << "\n";
		return -1;
	}

	if (pthread_attr_setdetachstate(&attr, PTHREAD_CREATE_DETACHED) != 0){
		std::cout << "[ERROR][THREAD][DETACH] " << strerror(errno) << "\n";
		return -1;
	}

	int master_socket, conn_id, len;
	sockaddr_in server, client;

	memset(&server, 0, sizeof(server));
	memset(&client, 0, sizeof(client));

	// creating master socket
	if ((master_socket = socket(AF_INET, SOCK_STREAM, 0)) == -1){
		std::cout << "[ERROR] CAN'T CREATE TO SOCKET\n";
		return -1;
	}else{
		std::cout << "[NOTE] SOCKET CREATED SERVER\n";
	}


	 // Prepare the sockaddr_in structure
	server.sin_family = AF_INET;
	server.sin_addr.s_addr = inet_addr(ADDRESS);
	server.sin_port = htons(port);

	len = sizeof(struct sockaddr_in);

	// binding address and port
	if (bind(master_socket, (struct sockaddr *)&server , sizeof(server)) == -1){
		std::cout << "[ERROR][BIND] " << strerror(errno) << "\n";
		return -1;
	}else{
		std::cout << "[NOTE] BIND " << ADDRESS << ":" << port << "\n";
	}

	// Listen on the socket, with 20 max connection requests queued
	if (listen(master_socket, QUEUE_CONNECTION) == -1){
		std::cout << "[ERROR][LISTEN] " << strerror(errno) << "\n";
		return -1;
	}else{
		std::cout << "[INFO] WAITING FOR INCOMING CONNECTIONS\n";
	}

	// infinity loop
	while (true){
		// accept new connections
		conn_id = accept(master_socket, (struct sockaddr*)&client, (socklen_t*)&len);
		id_connects.push_back(conn_id);


        // if connection acception failed
        if (conn_id == -1){
            std::cout << "[WARNING] CAN'T ACCEPT NEW CONNECTION\n";
        }else{
             // if connection limit reached
             if (connection >= CONCURRENT_CONNECTION){
                 std::cout << "[WARNING] CONNECTION LIMITE REACHED\n";
                 // send(conn_id, "server is busy. please try again later.", 39, 0);
                 close(conn_id); // close connection
             }else{
                     std::cout << "[INFO] NEW CONNECTION ACCEPTED FROM "
                               << inet_ntoa(client.sin_addr) << ":"
                               << ntohs(client.sin_port) << "\n";

                     // create new thread for new connection
                     if (pthread_create(&thread_id, &attr, connection_handler, new int(conn_id)) == -1){
                         // std::cout << "[WARNING][THREAD] " << strerror(errno) << "\n";
                         std::cout << "[WARNING] CAN'T CREATE NEW THREAD\n";
                         close(conn_id);
                     }else{
                        std::cout << "[INFO] NEW THREAD CREATED\n";
                        connection++; // increase connection count
                     }
                 }
             }
        }

    return 0;
}

// This will handle connection for each client
void *connection_handler(void *sock_fd){

	// byte size
	int read_byte = 0;

	// Get the socket descriptor
	int conn_id = *(int*)sock_fd;

	// request data
	char buffer[BUFFER_SIZE] = {0};


	// read response continue
	while (true){

		// clear buffer data
		memset(buffer, 0, BUFFER_SIZE);

		if((read_byte = recv(conn_id, buffer, BUFFER_SIZE, 0)) <= 0) break;

		std::cout << "------- Client ID : "  << conn_id << " ------------------- \n\n";
		std::cout << "[RECEIVE]: " << buffer << "\n";

		buffer[read_byte] = 0;

		for (unsigned int i = 0; i < id_connects.size(); ++i){
			int id = id_connects[i];

			// send to client
			if (send(id, buffer, read_byte, 0) > 0){
				std::cout << "[SEND]: " << buffer << "\n";
			}
			else{
				std::cout << "[WARNING][SEND]: " << strerror(errno) << "\n";
			}

			std::cout << "----------------------------------------------- \n";
		}

	}

	// terminate connection
	close(conn_id);
	std::cout << "[INFO] CONNECTION CLOSED\n";

	// decrease connection counts
	connection--;

	//id_connects.erase(std::remove(id_connects.begin(), id_connects.end(), conn_id), id_connects.end());
	id_connects.erase(std::find(id_connects.begin(), id_connects.end(), conn_id));

	// thread automatically terminate after exit connection handler
	std::cout << "[INFO] THREAD TERMINATED" << std::endl;

	delete (int*)sock_fd;


	// print line
	std::cout << "------------------------" << std::endl;

	// exiting
	pthread_exit(NULL);
}



