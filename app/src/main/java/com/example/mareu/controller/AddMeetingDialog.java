package com.example.mareu.controller;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatDialogFragment;
import com.example.mareu.DI.DI;
import com.example.mareu.R;
import com.example.mareu.events.AddMeetingEvent;
import com.example.mareu.model.Meeting;
import com.example.mareu.service.MeetingApiService;
import org.greenrobot.eventbus.EventBus;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

public class AddMeetingDialog extends AppCompatDialogFragment {
    private MeetingApiService service;
    private List<Meeting> mMeetings;

    private TextView mEditMeetingRoom;
    private TextView mEditMeetingDate;
    private TextView mEditMeetingStartHour;
    private TextView mEditMeetingEndHour;
    private EditText mEditMeetingSubject;
    private EditText mEditMeetingParticipants;

    private DatePickerDialog.OnDateSetListener mDateSetListener;
    private TimePickerDialog.OnTimeSetListener mTimeSetListenerBeginingMeeting;
    private TimePickerDialog.OnTimeSetListener mTimeSetListenerEndingMeeting;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        service = DI.getMeetingApiService();
        mMeetings = service.getMeetings();
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = Objects.requireNonNull(getActivity()).getLayoutInflater();
        View view = inflater.inflate(R.layout.add_meeting_dialog, null);

        mEditMeetingSubject = view.findViewById(R.id.edit_meeting_subject);
        mEditMeetingDate = view.findViewById(R.id.edit_meeting_date);
        mEditMeetingStartHour = view.findViewById(R.id.edit_meeting_start_hour);
        mEditMeetingEndHour = view.findViewById(R.id.edit_meeting_end_hour);
        mEditMeetingRoom = view.findViewById(R.id.edit_meeting_room);
        mEditMeetingParticipants = view.findViewById(R.id.edit_meeting_participant);

        builder.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });

        builder.setPositiveButton("Ajouter", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        builder.setView(view);

        AlertDialog dialog = builder.create();
        dialog.show();

        clickOnPositiveButton(dialog);
        getMeetingRoom();
        getMeetingDate();
        getMeetingStartHour();
        getMeetingEndHour();
        return dialog;
    }

    private void clickOnPositiveButton(final AlertDialog dialog) {
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String subject = mEditMeetingSubject.getText().toString();
                String date = mEditMeetingDate.getText().toString();
                String startHour = mEditMeetingStartHour.getText().toString();
                String endHours = mEditMeetingEndHour.getText().toString();
                String room = mEditMeetingRoom.getText().toString();
                String participants = mEditMeetingParticipants.getText().toString();
                int startingHourNewMeeting = convertMyStringToAnInt(startHour.toLowerCase());
                int endingHourNewMeeting = convertMyStringToAnInt(endHours.toLowerCase());
                String[] array = participants.split(",");
                List<String> listOfParticipants = Arrays.asList(array);

                boolean userSubject = TextUtils.isEmpty(mEditMeetingSubject.getText().toString().trim());
                boolean userDate = TextUtils.isEmpty(mEditMeetingDate.getText().toString().trim());
                boolean userHour = TextUtils.isEmpty(mEditMeetingStartHour.getText().toString().trim());
                boolean userRoom = TextUtils.isEmpty(mEditMeetingRoom.getText().toString().trim());
                boolean userMeeting = TextUtils.isEmpty(mEditMeetingParticipants.getText().toString().trim());

                if (userSubject || userDate || userHour || userMeeting ||userRoom) {
                    Toast.makeText(getContext(), "Tous les champs doivent être rempli", Toast.LENGTH_SHORT).show();
                } else {
                    checkIfRoomIsAvailable(room, date, startHour, endHours, subject, listOfParticipants, startingHourNewMeeting, endingHourNewMeeting);
                    dialog.dismiss();
                }
            }
        });
    }

    private void checkIfRoomIsAvailable(String room, String date, String startHour, String endHours, String subject, List<String> listOfParticipants, int startingHourNewMeeting, int endingHourNewMeeting ) {
        boolean isMeetingOkay = true;
        for (Meeting meeting : mMeetings) {
            if (meeting.getMeetingDate().toLowerCase().equals(date.toLowerCase()) && meeting.getMeetingRoom().toLowerCase().equals(room.toLowerCase())) {
                int meetingStartingHour = convertMyStringToAnInt(meeting.getMeetingStartingHour().toLowerCase());
                int meetingEndingHour = convertMyStringToAnInt(meeting.getMeetingEndingHour().toLowerCase());
                if (meetingStartingHour >= startingHourNewMeeting && meetingStartingHour >= endingHourNewMeeting || meetingEndingHour <= startingHourNewMeeting && meetingEndingHour <= endingHourNewMeeting) {
                } else {
                    Toast.makeText(getContext(), "Une réunion à déja lieu dans cette salle pendant ce temps", Toast.LENGTH_SHORT).show();
                    isMeetingOkay = false;
                }
            }
        }
        if (isMeetingOkay) {
            Meeting newMeeting = new Meeting(mMeetings.size() + 1, room, date, startHour, endHours, subject, listOfParticipants);
            EventBus.getDefault().post(new AddMeetingEvent(newMeeting));
        }
    }

    private void getMeetingRoom() {
        mEditMeetingRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder2 = new AlertDialog.Builder(getActivity());
                final String[] room = {"Salle 01", "Salle 02", "Salle 03", "Salle 04", "Salle 05", "Salle 06", "Salle 07", "Salle 08", "Salle 09", "Salle 10"};
                builder2.setItems(room, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mEditMeetingRoom.setText(room[which]);
                    }
                });
                AlertDialog dialog = builder2.create();
                dialog.show();
            }
        });
    }

    private void getMeetingDate() {
        mEditMeetingDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(getActivity(), android.R.style.Theme_Holo_Light_Dialog_MinWidth, mDateSetListener, year, month, day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month = month+1;
                String dayIs = convertMyIntToAString(day);
                String monthIs = convertMyIntToAString(month);
                String date = year+"/"+monthIs+"/"+dayIs;
                mEditMeetingDate.setText(date);
            }
        };
    }

    private void getMeetingStartHour() {
        mEditMeetingStartHour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar c = Calendar.getInstance();
                int hour = c.get(Calendar.HOUR);
                int minute = c.get(Calendar.MINUTE);
                TimePickerDialog timeDialog = new TimePickerDialog(getActivity(), android.R.style.Theme_Holo_Light_Dialog, mTimeSetListenerBeginingMeeting, hour, minute, true);
                timeDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                timeDialog.show();
            }
        });

        mTimeSetListenerBeginingMeeting = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                String hourIs = convertMyIntToAString(hour);
                String minuteIs = convertMyIntToAString(minute);
                String hourTime = hourIs+"h"+minuteIs;
                mEditMeetingStartHour.setText(hourTime);
            }
        };
    }

    private void getMeetingEndHour() {
        mEditMeetingEndHour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar c = Calendar.getInstance();
                int hour = c.get(Calendar.HOUR);
                int minute = c.get(Calendar.MINUTE);
                TimePickerDialog timeDialog = new TimePickerDialog(getActivity(), android.R.style.Theme_Holo_Light_Dialog, mTimeSetListenerEndingMeeting, hour, minute, true);
                timeDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                timeDialog.show();
            }
        });

        mTimeSetListenerEndingMeeting = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                String hourIs = convertMyIntToAString(hour);
                String minuteIs = convertMyIntToAString(minute);
                String hourTime = hourIs+"h"+minuteIs;
                mEditMeetingEndHour.setText(hourTime);
            }
        };
    }

    private String convertMyIntToAString(int myInt) {
        String myString;
        if (myInt<10) {
            myString = "0"+myInt;
        } else {
            myString = ""+myInt;
        }
        return myString;
    }

    private int convertMyStringToAnInt(String myString) {
        int myInt = 0;
        if (myString != null && myString.length() > 0) {
            String newString = myString.replace("h", "");
            myInt = Integer.parseInt(newString);
        }
        return myInt;
    }
}