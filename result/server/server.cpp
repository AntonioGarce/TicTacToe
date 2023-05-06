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
#define ADDRESS "10.99.4.85"
#define PORT 22222
#define CONCURRENT_CONNECTION 10
#define QUEUE_CONNECTION 20
#define BUFFER_SIZE 1024
#define THREAD_STACK_SIZE 65536

// number of clients in the rooms.
#define numClient  0
// number of lobby
#define numLobby  2
// number of room per lobby
#define numRoom  5
// number of available clients
#define availableClients  numLobby*numRoom*2;

enum commState {
	waitNickName,
	readNickName,
	sendLobbyState,
	sendRoomState,
	readLobby,
	readRoom,
	readyPlayer,
	readyStart,
	startGame,
	playGame,
	waitResponse,
	sendRoom
};

// state of Room ( 0:empty, 1:a client is ready 2:room is full )
int roomState[numLobby][numRoom];
int clientsAtRoom[numLobby][numRoom][2];

std::vector<int> id_connects;

// current connections
int connection = 0;
// true if client is first player, false if second palyer
bool firstPalyer = true;
// connection handler function
void *connection_handler(void* );

int main(int argc, char *argv[]){

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

	for (int i=0;i<numLobby;i++){
		for(int j=0;j<numRoom;j++){
			roomState[i][j] = 0;
		}
	}
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
	server.sin_port = htons(PORT);

	len = sizeof(struct sockaddr_in);

	// binding address and port
	if (bind(master_socket, (struct sockaddr *)&server , sizeof(server)) == -1){
		std::cout << "[ERROR][BIND] " << strerror(errno) << "\n";
		return -1;
	}else{
		std::cout << "[NOTE] BIND " << ADDRESS << ":" << PORT << "\n";
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

	// ture if it is in room, false if it is not in room
	bool hasRoom = false;
	// lobby where the client is in
	int lobby;
	// room where the client is in
	int room;
	// other client's id
	int client_id;
	// byte size
	int read_byte = 0;

	// Get the socket descriptor
	int conn_id = *(int*)sock_fd;

	//nickname of client
	char nickName[20] = {0};

	// read buffer
	char buffer[BUFFER_SIZE] = {0};
	
	// command buffer
	char cmd_buffer[50] = {0};

	// auxiliary command buffer
	char aux_cmd_buffer[50] = {0};

	// clear buffer data
	memset(buffer, 0, BUFFER_SIZE);

	// current communication state
	int state = 0;
	// previous communication state
	int prevState = 0;

	// read response continue
	while (true){

		if((read_byte = recv(conn_id, buffer, BUFFER_SIZE, 0)) <= 0) break;
		buffer[read_byte] = 0;
		std::cout<<"[Received] "<<buffer<<"\n";
		char *enterPosition;
		do{
			enterPosition = strchr(buffer,'\n');
			
			// strcpy(cmd_buffer,aux_cmd_buffer);
			if (enterPosition == NULL){
				strncat(cmd_buffer, buffer,read_byte);
				// cmd_buffer[read_byte]=0;
				// std::cout<<"waiting command:\n"<<cmd_buffer<<"\n";
			}else{
				//copy the bytes from buffer to command buffer
				strncat(cmd_buffer,buffer,enterPosition-buffer);
				//
				strncpy(buffer, enterPosition+1,read_byte+buffer-enterPosition);

				std::cout << "[Command]:"<<cmd_buffer<<"\n";
				switch (state)
				{
				case waitNickName:
					std::cout<<"[State] "<<"waitNickName\n";
					if(strcmp(cmd_buffer,"NickName") == 0){
						state = readNickName;
						std::cout<<"[NextState] "<<"readNickName\n";
					}			
					break;
				case readNickName:
					std::cout<<"[State] "<<"readNickName\n";
					strcpy(nickName,cmd_buffer);
					std::cout<<"[NickName]:"<<nickName<<"\n";
					
					// send to client
					if (send(conn_id, "success\n", 8, 0) > 0){
						std::cout << "[SEND]: " << "success" << "\n";
						//save previous state
						prevState = state;
						//go to next state
						state = waitResponse;
					}
					else{
						std::cout << "[WARNING][SEND]: " << strerror(errno) << "\n";
					}
					break;
				case sendLobbyState:
					std::cout<<"[State] "<<"sendLobbyState\n";
					if(!strcmp(cmd_buffer,"Lobby")){
						char strLobby[3];
						sprintf(strLobby,"%d\n",numLobby);
						if (send(conn_id, strLobby, strlen(strLobby), 0) > 0){
							std::cout << "[SEND number of Lobby]: " << strLobby<<"\n";
							//save previous state
							prevState = state;
							//go to next state
							state = waitResponse;
						}
						else{
							std::cout << "[WARNING][SEND]: " << strerror(errno) << "\n";
						}
					}
					break;
				case sendRoomState:
					std::cout<<"[State] "<<"sendRoomState\n";
					if(!strcmp(cmd_buffer,"Room")){
						char strRoom[3];
						sprintf(strRoom,"%d\n",numRoom);
						if (send(conn_id, strRoom, strlen(strRoom), 0) > 0){
							std::cout << "[SEND number of Room]: " << strRoom<<"\n";
							//save previous state
							prevState = state;
							//go to next state
							state = waitResponse;
						}
						else{
							std::cout << "[WARNING][SEND]: " << strerror(errno) << "\n";
						}
					}else{
						if (send(conn_id, "Error\n", 6, 0) > 0){
							std::cout << "[SEND number of Room]: " << "Error\n";
						}
						else{
							std::cout << "[WARNING][SEND]: " << strerror(errno) << "\n";
						}
					}
					
					break;		
				case readLobby:
					std::cout << "[State]: " << "readLobby\n";
					sscanf(cmd_buffer, "%d", &lobby);
					if (send(conn_id, "Room\n", 5, 0) > 0){
						std::cout << "[SEND]: " << "Room\n";
						//save previous state
						prevState = state;
						//go to next state
						state = readRoom;
					}
					else{
						std::cout << "[WARNING][SEND]: " << strerror(errno) << "\n";
					}
					break;	
				case readRoom:
					std::cout << "[State]: " << "readRoom\n";
					sscanf(cmd_buffer, "%d", &room);
					char res[20];
					if (room > numRoom) strcpy(res , "Error\n");
					else{
						switch (roomState[lobby][room])
						{
						case 0:
							strcpy(res , "First\n");
							//save previous state
							prevState = state;
							//go to next state
							// state = readyPlayer;

							//update roomState
							roomState[lobby][room] = 1;
							//
							hasRoom = true;
							// std::cout<<"[Next State] "<<"readyPlayer";
							// std::cout<<"[State] "<<"readyPlayer\n";
							clientsAtRoom[lobby][room][0] = conn_id;
							std::cout<<"[ClientsAtRoom0]"<<clientsAtRoom[lobby][room][0]<<"\n";
							firstPalyer = true;
							// client_id = clientsAtRoom[lobby][room][1]; 
							state = playGame;

							break;
						case 1:
							strcpy(res , "Second\n");
							//save previous state
							// prevState = state;
							// //go to next state
							// state = readyStart;
							
							// std::cout<<"[State] "<<"readyStart\n";
							clientsAtRoom[lobby][room][1] = conn_id;
							std::cout<<"[clientsAtRoom] "<<clientsAtRoom[lobby][room][0]<<","<<clientsAtRoom[lobby][room][1]<<"\n";
							state = playGame;
							firstPalyer = false;
							//update roomState
							roomState[lobby][room] = 2;		
							hasRoom = true;	

							break;
						default:
							strcpy(res , "Full\n");
							break;
						}
					} 
					if (send(conn_id, res, strlen(res), 0) > 0){
						std::cout << "[SEND]: " << res;
					}
					else{
						std::cout << "[WARNING][SEND]: " << strerror(errno) << "\n";
					}
					// for(;;){
					// 	if (send(conn_id, "POS:2,3\n", 8, 0) > 0){
					// 		std::cout << "[SEND]: " << "POS:2,3\n\n";
					// 		//save previous state
					// 		prevState = state;
					// 		//go to next state
					// 		// state = readRoom;
					// 	}
					// 	else{
					// 		std::cout << "[WARNING][SEND]: " << strerror(errno) << "\n";
					// 	}
					// }
					break;	
				case playGame:
					std::cout<<"[State] "<<"playGame\n";
					char* posX;
					char* posY;
					char cmd[10];
					char strX[2],strY[2];

					int x,y;
					posX = strchr(cmd_buffer,':');
					posY = strchr(cmd_buffer,',');
					// strncpy(cmd,cmd_buffer,posX-cmd_buffer);
					if(posX && posY){
						if(clientsAtRoom[lobby][room][0] == conn_id){
							client_id = clientsAtRoom[lobby][room][1];
							std::cout<<"firstplayer";
						}else{
							client_id = clientsAtRoom[lobby][room][0];
							std::cout<<"secondplayer";
						}
						std::cout << "[conn_id]: " << conn_id<<"\n";
						std::cout << "[client_id]: " << client_id<<"\n";
						strcat(cmd_buffer,"\n");
						if(!strncmp(cmd_buffer,"DEF",3)){
							roomState[lobby][room] = 0;
							state = readLobby;
							std::cout << "start new def state\n";
						}else if (send(client_id, cmd_buffer, strlen(cmd_buffer), 0) > 0){
							std::cout << "[SEND]: " << cmd_buffer;
							if(!strncmp(cmd_buffer,"WIN",3)){
								roomState[lobby][room] = 0;
								if (send(conn_id, "END\n", 4, 0) > 0){
									std::cout << "[SEND]: "<<"END\n";
								}else{
									std::cout << "[WARNING][SEND]: " << strerror(errno) << "\n";
								}
								state = readLobby;
								std::cout << "start new win state\n";
							} 
						}
						else{
							std::cout << "[WARNING][SEND]: " << strerror(errno) << "\n";
						}
					}
					break;

				case waitResponse:		//wait and process response from client
					std::cout<<"[State] "<<"wait Response For "<<prevState<<"\n";
					switch (prevState)
					{
						case readNickName:	//previous state is readNickName
							if(strcmp(cmd_buffer,"success") == 0){
								state = sendLobbyState;
								std::cout<<"[NextState] "<<"SendLobbyState\n";	
							}	
							break;
						case sendLobbyState:
							int numlob;
							sscanf(cmd_buffer, "%d", &numlob);
							std::cout<<"[Lobby response] "<<numlob<<"\n";
							if(numLobby == numlob){
								state = sendRoomState;
								std::cout<<"[NextState] "<<"SendRoomState\n";	
							}
							break;
						case sendRoomState:
							int numroom;
							sscanf(cmd_buffer, "%d", &numroom);
							std::cout<<"[Room response] "<<numroom<<"\n";
							if(numRoom == numroom){
								state = readLobby;
								std::cout<<"[NextState] "<<"readLobby\n";	
							}
							break;							
						default:
							break;
					}
			
					break;
				default:
					break;
				}
				cmd_buffer[0] = 0;

			}
		}while(enterPosition);
		
		std::cout << "------- Client ID : "  << conn_id << " ------------------- \n\n";
		
	}

	if (hasRoom) {
		roomState[lobby][room] -= 1; 
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



