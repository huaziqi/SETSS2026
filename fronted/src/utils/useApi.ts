import {ref} from 'vue';
import axios from 'axios';

export default function useApi() { 


    const data = ref();
    const loading = ref(false);
    const error = ref<String | null>(null);

    const buildHeaders = () => {
        const accessToken = "ok"
        const headers: Record<string, string> = {};
        if(accessToken) {
            headers['Authorization'] = `Bearer ${accessToken}`;
        }

        return headers;
    }
    const fetchData = async (url: string) => {
        console.log(url);

        loading.value = true;
        error.value = null;
        try{
            const headers = buildHeaders();
            const response = await axios.get(url, {headers});
            data.value = response.data;
        }
        catch (err){
            error.value = (err as Error).message;
        }
        finally{
            loading.value = false;
        }
    }
    const postData = async (url: string, payload: any) => {
        loading.value = true;
        error.value = null;
        try{
            const headers = buildHeaders();
            const response = await axios.post(url, payload, {headers});
            data.value = response.data;
        }
        catch (err){
            error.value = (err as Error).message;
        }
        finally{
            loading.value = false;
        }
    }
    return {data, loading, error, fetchData, postData};

}
