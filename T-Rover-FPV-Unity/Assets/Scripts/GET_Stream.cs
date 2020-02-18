using UnityEngine;
using UnityEngine.UI;
using UnityEngine.Networking;
using System.Collections;
 
 public class GET_Stream : MonoBehaviour
{
    public Texture texture;
    public InputField urlInput;
    private string url;

    public float GetRate = 0.15f;
    private float nextGet = 0;

    IEnumerator GetTexture()
    {
        //url = urlInput.text;
        Renderer renderer = GetComponent<Renderer>();
        UnityWebRequest www = UnityWebRequestTexture.GetTexture("http://192.168.1.176:8080/shot.jpg");
        yield return www.Send();
        renderer.material.mainTexture = ((DownloadHandlerTexture)www.downloadHandler).texture;
    }

    void Update()
    {
        if (Time.time > nextGet)
        {
            StartCoroutine(GetTexture());
            nextGet = Time.time + GetRate;
            //Debug.Log("Hello");
        }
    }
}