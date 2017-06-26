#ifndef _JASON_PLUGIN_HH_
#define _JASON_PLUGIN_HH_

#include <gazebo/gazebo.hh>
#include <gazebo/physics/physics.hh>
#include <gazebo/gazebo_config.h>
#include <gazebo/transport/transport.hh>
#include <gazebo/msgs/msgs.hh>
#include <iostream>
#include <string>
#include "jros.h"

#define PI 3.14159265358979323846
#define EPSILON 0.01
#define MOVE_SPEED 0.1 // m/s
#define ROTATION_SPEED 15 //graus/s
#define UPDATE_RATE 100.0
//Definidos no .sdf
#define WHEEL_DIAM 0.070
#define WHEEL_SEP .230
#define ACCEL 0.0


namespace gazebo
{
  class JasonPlugin : public ModelPlugin
  {
    public: JasonPlugin() {}
public:
    void move(double distance){// metros
      linearVel = MOVE_SPEED;
      distanceGoal = distance;
      distanceTime = fabs(distance/MOVE_SPEED);
    }
public:
    void rotate(double ang){// graus | graus/s
      angularVel = fabs(ROTATION_SPEED * 2 * PI / 360);
      angleGoal = ang;
      rotationTime = fabs(ang / ROTATION_SPEED);
    }

  public:
    void jasonCB(int id,std::string agent, std::string action, std::vector<std::string> parameters){
      std::cout << "entrou jasonCB" << std::endl;
      std::cout << "New Action!!!!\n";
      std::cout << "Action id: " << id << std::endl;
      std::cout << "Agent name: " << agent << std::endl;
      std::cout << "Action name: " << action << std::endl;
      std::cout << action << "(";
      for(int i = 0; i < parameters.size();i++)
        std::cout << parameters[i];
      std::cout << ")\n";
      std::cout << "1\n";
      if(id != this->lastId){
        std::cout << "2\n";
        if(action == "move"){
          std::cout << "movendo!\n";
          move(stoi(parameters[0]));
        }
        if(action == "rotate"){
          rotate(stoi(parameters[0]));
        }
        this->lastId = id;
      }
  }
  public: void getVel(){
    //std::cerr << "Vel:" << linearVel << std::endl;
    boost::mutex::scoped_lock scoped_lock(lock);
    double lv = linearVel;
    double av = angularVel;
    wspeedL = lv + av * WHEEL_SEP/2.0;
    wspeedR = lv - av * WHEEL_SEP/2.0;
  }
  public: void Update(){
    //if(jros.getConfirmation() == 1){
    common::Time currTime = model->GetWorld()->GetSimTime();
    double deltaTime = (currTime - lastTime).Double();
    if(deltaTime > updateRate){
      if(angleGoal != 0){
        //std::cout << "rotationTime:" << rotationTime << "  -  " << "updateRate:" << updateRate << std::endl;
        rotationTime -= updateRate;
        if(rotationTime <= 0){
          angularVel = 0;
          angleGoal = 0;
          jros.sendConfirmation("rotate");
        }
      }
      if(distanceGoal != 0){
        //std::cout << "distanceTime:" << distanceTime << "  -  " << "updateRate:" << updateRate << std::endl;
        distanceTime -= updateRate;
        if(distanceTime <= 0){
          linearVel = 0;
          distanceGoal = 0;
          std::cout << "moveu !\n";
          jros.sendConfirmation("move");
        }
      }
      getVel();
      double currSpeedL,currSpeedR;
      currSpeedL = this->jleft->GetVelocity(0)*(WHEEL_DIAM/2.0);
      currSpeedR = this->jright->GetVelocity(0)*(WHEEL_DIAM/2.0);
      double epL = fabs(wspeedL - currSpeedL);
      double epR = fabs(wspeedR - currSpeedR);
      if((ACCEL == 0) || (epL < EPSILON) || (epR < EPSILON)){
        this->model->GetJointController()->SetVelocityTarget(this->jleft->GetScopedName(),wspeedL/(WHEEL_DIAM/2.0));
        this->model->GetJointController()->SetVelocityTarget(this->jright->GetScopedName(),wspeedR/(WHEEL_DIAM/2.0));
      }else{
        if(wspeedL >= currSpeedL){
          wspeedLAux += fmin(wspeedL - currSpeedL,ACCEL * deltaTime);
        }else{
          wspeedLAux += fmax(wspeedL - currSpeedL,-ACCEL * deltaTime);
        }

        if(wspeedR >= currSpeedR){
          wspeedRAux += fmin(wspeedR - currSpeedR,ACCEL * deltaTime);
        }else{
          wspeedRAux += fmax(wspeedR - currSpeedR,-ACCEL * deltaTime);
        }
        this->model->GetJointController()->SetVelocityTarget(this->jleft->GetScopedName(),wspeedLAux/(WHEEL_DIAM/2.0));
        this->model->GetJointController()->SetVelocityTarget(this->jright->GetScopedName(),wspeedRAux/(WHEEL_DIAM/2.0));
      }
      lastTime += common::Time(updateRate);
    }
  //}
  }


    public: virtual void Load(physics::ModelPtr _model, sdf::ElementPtr _sdf)
    {

      if(_model->GetJointCount() != 2){
        std::cerr << "Invalid joint count!\n";
        return;
      }
      std::string str;
      int argc = 0;
      char **argv = NULL;
      //JROS jros;
      //pjros = &jros;
      jros.init(argc,argv,_model->GetName(),boost::bind(&JasonPlugin::jasonCB,this,_1,_2,_3,_4));//alterar quando for simular MAS
      this->v = _model->GetJoints();
      this->model = _model;
      this->jleft = v[0];
      this->jright = v[1];
      std::cerr << "\nJoint Left:" << this->jleft->GetName();
      std::cerr << "\nJoint Right:" << this->jright->GetName();
      std::cerr << "\nJason Plugin loaded![" << _model->GetName() << "]\n";
      this->pid = common::PID(0.1, 0, 0);
      this->model->GetJointController()->SetVelocityPID(this->jleft->GetScopedName(),this->pid);
      this->model->GetJointController()->SetVelocityPID(this->jright->GetScopedName(),this->pid);
      if(UPDATE_RATE > 0.0) this->updateRate = 1.0/UPDATE_RATE;
      else this->updateRate = 0.0;
      lastTime = model->GetWorld()->GetSimTime();
      this->updateConn = event::Events::ConnectWorldUpdateBegin(boost::bind(&JasonPlugin::Update,this));
      //this->move(1);
      //this->updateConn2 = pjros->getTest();

      //this->rotate(360);//graus
    }
  private: double wspeedR = 0;
  private: double wspeedL = 0;
  private: double wspeedRAux = 0;
  private: double wspeedLAux = 0;
  private: double updateRate;
  private: boost::mutex lock;
  private: physics::Joint_V v;
  private: physics::ModelPtr model;
  private: physics::JointPtr jleft,jright;
  private: common::PID pid;
  private: common::Time lastTime;
  private: double distanceGoal = 0;
  private: double distanceTime = 0;
  private: double linearVel = 0;
  private: double angularVel = 0;
  private: double angleGoal = 0;
  private: double rotationTime = 0;
private: JROS jros;
private:  int lastId = -1;
  event::ConnectionPtr updateConn;
  event::ConnectionPtr updateConn2;
  };
  GZ_REGISTER_MODEL_PLUGIN(JasonPlugin)
}
#endif
